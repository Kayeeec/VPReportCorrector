package org.umlreviewer.sync.git

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import javafx.application.Platform
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.*
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.RepositoryState
import org.eclipse.jgit.transport.*
import org.eclipse.jgit.util.FS
import org.umlreviewer.utils.file.dialogs.RememberChoice
import org.umlreviewer.sync.ReinitializationParameters
import org.umlreviewer.sync.SyncService
import org.umlreviewer.sync.git.exceptions.GitSyncServiceException
import org.umlreviewer.sync.git.settings.GitProtocol
import org.umlreviewer.sync.git.settings.GitSettingsModel
import org.umlreviewer.utils.*
import org.umlreviewer.utils.Helpers.getWorkingDirectory
import org.umlreviewer.utils.file.FileConflictChoice
import org.umlreviewer.utils.file.copyFiles
import org.umlreviewer.utils.file.deleteDirectoryStream
import org.umlreviewer.utils.file.listAll
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger

class GitSyncService: SyncService {
    private val log by lazy { Logger.getLogger(this.javaClass.name) }
    private val workDir = getWorkingDirectory()
    private val localGitFolder = if (workDir == null) null else Paths.get(workDir.toFile().absolutePath, ".git")
    private val git: Git?
    private val gitSettings = GitSettingsModel()

    private val sshSessionFactory: SshSessionFactory by lazy {
        object : JschConfigSessionFactory() {
            @Throws(JSchException::class)
            override fun createDefaultJSch(fs: FS?): JSch {
                val defaultJSch: JSch = super.createDefaultJSch(fs)
                if (gitSettings.passphrase.isNotEmpty()) {
                    defaultJSch.addIdentity(gitSettings.privateKeyPath, gitSettings.passphrase)
                } else {
                    defaultJSch.addIdentity(gitSettings.privateKeyPath)
                }
                return defaultJSch
            }
        }
    }

    companion object {
        private const val BRANCH = "master"
        private const val REMOTE = "origin"
        private val pushRejectedStatuses = setOf(
            RemoteRefUpdate.Status.REJECTED_NODELETE,
            RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD,
            RemoteRefUpdate.Status.REJECTED_OTHER_REASON,
            RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED,
        )
    }

    init {
        gitSettings.load()
        git = if (localRepositoryExists()) {
            log.info("Opening existing git repository.")
            Git.open(localGitFolder!!.toFile())
        } else {
            initializeNewRepository()
        }
    }

    /**
     * Initialises new git repository and pulls files from the remote.
     *
     * @return git  a JGit representation of the created repository, null if it could not be initialized
     */
    private fun initializeNewRepository(): Git? {
        log.info("Creating new local git repository.")
        if (!canInitializeNewRepository()) return null
        val nGit = Git.init().setDirectory(workDir!!.toFile()).call()
        checkRepositoryInitializedAndClean(nGit)
        setUserConfig(nGit)
        nGit.remoteAdd()?.setName("origin")?.setUri(URIish(gitSettings.repoUrl))?.call()
        nGit.fetch().addAuthentication().call()
        log.info("Initial file synchronisation.")
        if (nGit.hasUntrackedFiles()) {
            log.info("Folder has untracked files")
            val tempDir = stashWorkdirContentsToTemp()
            gitAddAllAndCommit(nGit, "initial") // will be deleted along with the added files by pullRebase(...)
            val conf = nGit.repository.config
            conf.setString(CONFIG_BRANCH_SECTION, BRANCH, "remote", REMOTE)
            conf.setString(CONFIG_BRANCH_SECTION, BRANCH, "merge", "refs/heads/${BRANCH}")
            conf.save()
            pullRebaseAndContinue(nGit)
            unstashWorkdirContentsFromTemp(tempDir)
        } else {
            log.info("No untracked files.")
            nGit.checkout()!!
                .setCreateBranch(true)
                .setName(BRANCH)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setStartPoint("$REMOTE/$BRANCH")
                .call()
        }
        return nGit
    }

    private fun setUserConfig(nGit: Git) {
        val config = nGit.repository.config
        var doSave = false
        if (gitSettings.configUserName.isNotEmpty()) {
            config.setString("user", null, "name", gitSettings.configUserName)
            doSave = true
        }
        if (gitSettings.configUserEmail.isNotEmpty()) {
            config.setString("user", null, "email", gitSettings.configUserEmail)
            doSave = true
        }
        if (doSave) config.save()
    }


    /**
     * Two-way synchronisation of the working directory with remote git repository.
     * Pulls the commits from the remote repo and rebases the local changes/commits on top of them (using a workaround -
     * stashes changes to a temporary folder and then copies them back, due to a JGit bug
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=501111)
     * Local changes take precedence.
     */
    override fun sync(fxTask: FXTask<*>) {
        if (!canSync()) return
        log.info("Syncing files with remote git repository...")
        val status = git!!.status().call()
        if (status.isClean) {
            fxTask.updateMessage("Pulling remote changes...")
            pullRebaseAndContinue(git)
        } else {
            val stashTempDir = stashChanges(status)
            git.reset().setMode(ResetCommand.ResetType.HARD).call()
            git.clean().setForce(true).call()
            fxTask.updateMessage("Pulling remote changes...")
            pullRebaseAndContinue(git)
            fxTask.updateMessage("Applying local changes...")
            unstashWorkdirContentsFromTemp(stashTempDir)
            deleteRemovedFiles(status)
            gitAddAllAndCommit(git, getSyncCommitMessage())
            fxTask.updateMessage("Pushing to remote repository...")
            pushAndResetIfRejected(git)
        }
    }

    private fun getSyncCommitMessage() = "${gitSettings.configUserName} (${gitSettings.configUserEmail}) - sync"

    private fun deleteRemovedFiles(status: Status) {
        if (workDir == null) return
        val workDirAbs = workDir.toFile().absolutePath
        status.removed.forEach {
            deleteRelativePathIfExists(workDirAbs, it)
        }
        status.missing.forEach {
            deleteRelativePathIfExists(workDirAbs, it)
        }
    }

    private fun deleteRelativePathIfExists(basePath: String, relativePath: String) {
        try {
            val path = Paths.get(basePath, relativePath)
            if (Files.exists(path)) {
                deleteDirectoryStream(path)
            }
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
        }
    }

    override fun shouldReinitializeAndOrDispose(): ReinitializationParameters {
        val newWorkingDir = getWorkingDirectory()
        val newSettings = GitSettingsModel()
        newSettings.load()
        val workdirChanged = newWorkingDir != workDir
        val repoUrlChanged = newSettings.repoUrl != gitSettings.repoUrl
                || newSettings.repoUrl != git?.repository?.config?.getString("remote", REMOTE, "url")
        return ReinitializationParameters(
            reinitialize = workdirChanged || newSettings != gitSettings,
            dispose = workdirChanged || repoUrlChanged
        )
    }

    override fun dispose() {
        log.info("Trying to dispose of previous git repository.")
        if (localGitFolder != null && Files.exists(localGitFolder)) {
            try {
                deleteDirectoryStream(localGitFolder)
            } catch (e: Throwable) {
                throw GitSyncServiceException("Failed to dispose of the '.git' directory.", e)
            }
        }
    }

    private fun pushAndResetIfRejected(nGit: Git) {
        val pushResults = nGit.push()
            .addAuthentication()
            .setProgressMonitor(LoggingProgressMonitor())
            .call()
        val anyRefsRejected = pushResults.any { pr -> pr.remoteUpdates.any { ru -> pushRejectedStatuses.contains(ru.status) } }
        if (anyRefsRejected) {
            val messages = pushResults.map { it.messages }.joinToString("\n")
            resetLastSyncCommitIfExists(nGit)
            Platform.runLater {
                warning(
                    title = "Warning",
                    header = "Sync failed - push to remote was rejected. Try running the sync again.",
                    content = if (messages.isNotBlank()) "Messages:\n${messages}" else null
                )
            }
        }
    }

    private fun resetLastSyncCommitIfExists(nGit: Git) {
        val latestCommit = nGit.log().setMaxCount(1).call().iterator().next()
        if (latestCommit.fullMessage == getSyncCommitMessage()) {
            log.info("Resetting last sync commit.")
            nGit.reset().setMode(ResetCommand.ResetType.SOFT).setRef("HEAD~1").call()
        }
    }

    private fun pullRebaseAndContinue(nGit: Git) {
        val pullResult = nGit.pull()
            .addAuthentication()
            .setProgressMonitor(LoggingProgressMonitor())
            .setRebase(true)
            .call()
        logFailedPullResult(pullResult)
        if (!pullResult.rebaseResult.status.isSuccessful && nGit.repository.repositoryState != RepositoryState.SAFE) {
            nGit.rebase().setOperation(RebaseCommand.Operation.CONTINUE)
        }
    }

    private fun logFailedPullResult(result: PullResult) {
        if (!result.isSuccessful) {
            log.severe("Pulling changes from remote failed.\n" +
                    "Fetch message: ${result.fetchResult.messages}\n" +
                    "Merge status: ${result.mergeResult.mergeStatus.name}\n" +
                    "Rebase status: ${result.rebaseResult.status.name}"
            )
        }
    }

    private fun gitAddAllAndCommit(nGit: Git, message: String) {
        nGit.add().addFilepattern(".").call()
        nGit.add().setUpdate(true).addFilepattern(".").call()
        nGit.commit().setMessage(message).setAuthor(gitSettings.username, gitSettings.configUserEmail).call()
    }

    private fun localRepositoryExists(): Boolean {
        return localGitFolder != null && Files.exists(localGitFolder)
    }

    private fun checkRepositoryInitializedAndClean(nGit: Git) {
        requireNotNull(nGit.repository?.findRef(Constants.HEAD))
        assert(nGit.status()?.call()?.isClean == true)
    }

    private fun canInitializeNewRepository(): Boolean {
        val pre = "Cannot initialize git service -"
        return (customAssert(workDir != null, "$pre Working directory is not set.", false)
                && customAssert(localGitFolder != null && !Files.exists(localGitFolder),
            "$pre Working directory already has a .git folder.")
                && customAssert(gitSettings.hasCorrectRemoteRepoSettings(),
            "$pre Incorrect git repository settings. Please check the application settings.", false)
                )
    }

    private fun canSync(): Boolean {
        val pre = "Cannot sync -"
        return customAssert(git != null,
            "$pre GitSyncService was not properly initialized (git is null). Please check the settings.")
                && customAssert(gitSettings.hasCorrectRemoteRepoSettings(),
            "$pre Incorrect git repository settings. Please check the settings.")
    }

    private fun customAssert(predicate: Boolean, onFalseMessage: String, doThrow: Boolean = true): Boolean {
        if (!predicate) {
            log.severe(onFalseMessage)
            error(onFalseMessage)
        }
        return predicate
    }

    private fun <C: GitCommand<T>, T> TransportCommand<C, T>.addAuthentication(): C {
        return when (gitSettings.method) {
            GitProtocol.HTTPS -> this.setCredentialsProvider(
                UsernamePasswordCredentialsProvider(gitSettings.username, gitSettings.password)
            )
            GitProtocol.SSH -> this.setTransportConfigCallback { t ->
                try {
                    (t as SshTransport).sshSessionFactory = sshSessionFactory
                } catch (e: ClassCastException) {
                    throw GitSyncServiceException(
                        "Cannot establish SSH transport. Please check your settings if the repository URL is correct.",
                        e
                    )
                }
            }
        }
    }

    private fun Git.hasUntrackedFiles(): Boolean {
        return this.status().call().untracked.isNotEmpty()
    }

    /**
     * Creates a temporary directory and copies the contents of [workDir] into it.
     * @return temDir   File representation if temporary directory serving as a copy of [workDir], null if [workDir] is null
     */
    private fun stashWorkdirContentsToTemp(): File? {
        if(workDir == null) return null
        val tempDir = createTempDir("VPRC-workdir-stash")
        val files = workDir.listAll()
        copyFiles(tempDir, files, RememberChoice(
            directory = FileConflictChoice.REPLACE_OR_MERGE,
            file = FileConflictChoice.REPLACE_OR_MERGE
        )
        )
        return tempDir
    }

    /**
     * Copies the contents of [tempDir] to the [workDir], overwriting conflicting files and deletes the [tempDir].
     * @param tempDir   directory to copy contents from
     */
    private fun unstashWorkdirContentsFromTemp(tempDir: File?) {
        if (tempDir == null || workDir == null) return
        copyFiles(workDir.toFile(), tempDir.toPath().listAll(), RememberChoice(
            directory = FileConflictChoice.REPLACE_OR_MERGE,
            file = FileConflictChoice.REPLACE_OR_MERGE
        )
        )
        deleteDirectoryStream(tempDir.toPath())
    }

    private fun stashChanges(status: Status): File? {
        if(workDir == null
            || status.untracked.size + status.added.size + status.changed.size + status.modified.size == 0) return null
        val workDirAbs = workDir.toFile().absolutePath
        val tempDir = createTempDir("VPRC-sync-changes-stash")
        val tempDirAbs = tempDir.absolutePath
        status.untracked.forEach { copyToTemp(workDirAbs, it, tempDirAbs) }
        status.added.forEach { copyToTemp(workDirAbs, it, tempDirAbs) }
        status.changed.forEach { copyToTemp(workDirAbs, it, tempDirAbs) }
        status.modified.forEach { copyToTemp(workDirAbs, it, tempDirAbs) }
        return tempDir
    }

    private fun copyToTemp(workDirAbs: String, fileRelative: String, tempDirAbs: String) {
        val src = Paths.get(workDirAbs, fileRelative)
        val destDir = Paths.get(tempDirAbs, fileRelative)
        FileUtils.copyFile(src.toFile(), destDir.toFile(), true)
    }
}
