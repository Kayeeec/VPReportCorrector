package org.vpreportcorrector.utils

import org.vpreportcorrector.utils.Helpers.getWorkingDirectory
import java.io.File
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths

object FileTreeHelpers {
    val REGEX_WEEK_FOLDER = Regex(
        "(^(week|týden|týždeň|tyden|tyzden)(\\s|_|-)*0*\\d+\$)|(^0*\\d+(\\s|_|-)*(week|týden|týždeň|tyden|tyzden)\$)",
        RegexOption.IGNORE_CASE
    )
    val REGEX_TEAM_FOLDER = Regex(
        "(^(team|tým|tím|tym|tim)(\\s|_|-)*0*\\d+\$)|(^0*\\d+(\\s|_|-)*(team|tým|tím|tym|tim)\$)",
        RegexOption.IGNORE_CASE
    )
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

    /**
     * Tests if given folder name fits week or team folder name pattern and returns an appropriate message.
     *
     * Expected folder structure: (first 3 levels in the working directory are set)
     *
     *  A) WORKING DIR/YEAR/TUTOR NAME/SEMINAR GROUP/WEEK #/TEAM #
     *  B) WORKING DIR/YEAR/TUTOR NAME/SEMINAR GROUP/TEAM #/WEEK #
     */
    fun getFolderNameInfoOrWarning(location: File, folderName: String): FolderNameMessage? {
        if (folderName.isBlank()) return null
        val workdir = getWorkingDirectory() ?: return null
        val newFolderPath = Paths.get(location.canonicalPath, folderName)
        return when(getFileLevel(newFolderPath.toFile(), workdir.toFile())) {
            4 -> shouldMatchWeekOrTeam(folderName)
            5 -> shouldMatchWeekInTeamOrTeamInWeek(folderName, newFolderPath)
            else -> null
        }
    }

    private fun shouldMatchWeekInTeamOrTeamInWeek(folderName: String, newFolderPath: Path): FolderNameMessage? {
        val parentName = newFolderPath.parent.toFile().name
        if (matchesWeek(parentName)) {
            if (!matchesTeam(folderName)) {
                return FolderNameMessage(
                    "Name does NOT match a team folder name pattern (e.g. team6', 'tým 3'...).",
                    FolderMessageType.WARNING
                )
            }
            return FolderNameMessage(
                "Name matches a team folder name pattern.",
                FolderMessageType.SUCCESS
            )
        }
        else if (matchesTeam(parentName)) {
            if (!matchesWeek(folderName)) {
                return FolderNameMessage(
                    "Name does NOT match a week folder name pattern (e.g. 'week 1', 'týden2'...).",
                    FolderMessageType.WARNING
                )
            }
            return FolderNameMessage(
                "Name matches a week folder name pattern.",
                FolderMessageType.SUCCESS
            )
        }
        return null
    }

    private fun shouldMatchWeekOrTeam(folderName: String): FolderNameMessage {
        if (matchesTeam(folderName)) {
            return FolderNameMessage(
                "Name matches a team folder name pattern.",
                FolderMessageType.SUCCESS
            )
        } else if (matchesWeek(folderName)) {
            return FolderNameMessage(
                "Name matches a week folder name pattern.",
                FolderMessageType.SUCCESS
            )
        }
        return FolderNameMessage(
            "Name does NOT match week or team folder name pattern (e.g.: 'week 1', 'týden2', 'team6', 'tým 3'...).",
            FolderMessageType.WARNING
        )
    }

    private fun matchesWeek(folderName: String): Boolean {
        return REGEX_WEEK_FOLDER.matches(folderName)
    }

    private fun matchesTeam(folderName: String): Boolean {
        return REGEX_TEAM_FOLDER.matches(folderName)
    }

    /**
     * Computes the depth/level of [file] within the [base] folder.
     * E.g.: For base = "dir" and file = "dir/subdir/subsubdir" returns 2.
     *
     * @param file a file or folder within the [base]
     * @param base a File denoting a folder
     * @return number denoting how deep within the [base] folder the [file] is located.
     */
    private fun getFileLevel(file: File, base: File): Int {
        if (!file.toPath().isDescendantOf(base.toPath())) error("$file is not a descendant file of $base")
        if (!base.isDirectory) error("$base is not a directory")
        var tmp: File? = file.relativeTo(base)
        var l = 0
        while (tmp != null) {
            tmp = tmp.parentFile
            l++
        }
        return l
    }
}

enum class FolderMessageType {
    SUCCESS, WARNING
}

data class FolderNameMessage(
    val message: String,
    val type: FolderMessageType,
)
