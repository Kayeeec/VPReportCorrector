package org.vpreportcorrector.utils

import org.vpreportcorrector.settings.SettingsPreferencesKey
import org.vpreportcorrector.sync.RemoteRepo
import org.vpreportcorrector.utils.AppConstants.PREFERENCES_NODE
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.prefs.Preferences

fun preferencesHelper(op: Preferences.() -> Unit) {
    val node = Preferences.userRoot().node(PREFERENCES_NODE)
    op(node)
}

fun getWorkingDirectory(): Path? {
    var workingDir = ""
    preferencesHelper {
        sync()
        workingDir = get(SettingsPreferencesKey.WORKING_DIRECTORY, "")
    }
    return if (workingDir.isNotEmpty()) Paths.get(workingDir) else null
}

fun getRemoteRepositoryType(): RemoteRepo? {
    var result = ""
    preferencesHelper {
        sync()
        result = get(SettingsPreferencesKey.REMOTE_REPOSITORY, RemoteRepo.default.name)
    }
    return enumValueOrNull(result)
}

/**
 * Removes all files in the '.data' directory that don't have and equivalent PDF file in the working directory.
 */
fun cleanDataDirectory() {
    val workDir = getWorkingDirectory() ?: return
    val dataDir = Paths.get(workDir.toFile().canonicalPath, AppConstants.DATA_FOLDER_NAME)
    if (!Files.exists(dataDir)) return

    Files.walk(dataDir).use { walk ->
        walk.sorted(Comparator.reverseOrder())
            .filter { !hasEquivalentPdfOrDirectory(it, workDir, dataDir) }
            .forEach {
                if (it !== dataDir) it.toFile().delete()
            }
    }
}

private fun hasEquivalentPdfOrDirectory(path: Path?, workDir: Path, dataDir: Path): Boolean {
    if (path == null || path == dataDir) return true
    val filename = if (path.toFile().isDirectory) path.toFile().nameWithoutExtension
    else "${path.toFile().nameWithoutExtension}.pdf"
    val relParentPath = path.parent.toFile().relativeTo(dataDir.toFile())
    val ekvFile = Paths.get(workDir.toFile().canonicalPath, relParentPath.toString(), filename)
    return fileExistsNameCaseInsensitive(ekvFile)
}

/**
 * Tests if file denoted by [path] exists and does not care about the case of the filename.
 * E.g.: returns true for path 'path/to/file.PDF' if there is file 'path/to/File.pdf'
 */
private fun fileExistsNameCaseInsensitive(path: Path): Boolean {
    return try {
        val filename = path.toFile().name
        val siblingsAndSelfNames = path.parent.listAll().map { it.toFile().name }
        siblingsAndSelfNames.firstOrNull { it.equals(filename, true) } !== null
    } catch (e: NoSuchFileException) {
        false
    }
}
