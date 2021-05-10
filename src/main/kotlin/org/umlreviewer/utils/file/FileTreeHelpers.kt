package org.umlreviewer.utils.file

import org.umlreviewer.utils.AppConstants
import org.umlreviewer.utils.PreferencesHelper
import org.umlreviewer.utils.PreferencesHelper.getWorkingDirectory
import java.io.File
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KFunction1

object FileTreeHelpers {
    val REGEX_WEEK_FOLDER = Regex(
        "((week|týden|týždeň|tyden|tyzden)(\\s|_|-)*0*\\d+)|(0*\\d+(\\s|_|-)*(week|týden|týždeň|tyden|tyzden))",
        RegexOption.IGNORE_CASE
    )
    val REGEX_TEAM_FOLDER = Regex(
        "((team|tým|tím|tym|tim)(\\s|_|-)*0*\\d+)|(0*\\d+(\\s|_|-)*(team|tým|tím|tym|tim))",
        RegexOption.IGNORE_CASE
    )
    const val SEMINAR_GROUP_LEVEL = 3
    const val TUTOR_LEVEL = 2
    const val YEAR_LEVEL = 1
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

    fun getJsonFilePath(path: Path): Path {
        val workdir = PreferencesHelper.getWorkingDirectory()?.toFile() ?: error("Can't get relative path because working directory is null.")
        val relativeParent: File? = path.toFile().relativeTo(workdir).parentFile
        val jsonFileName = "${path.toFile().nameWithoutExtension}.json"
        return Paths.get(workdir.absolutePath, AppConstants.DATA_FOLDER_NAME, relativeParent?.path ?: "" , jsonFileName)
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

    fun matchesWeek(folderName: String): Boolean {
        return REGEX_WEEK_FOLDER.containsMatchIn(folderName)
    }


    fun matchesTeam(folderName: String): Boolean {
        return REGEX_TEAM_FOLDER.containsMatchIn(folderName)
    }

    /**
     * Computes the depth/level of [file] within the [base] folder.
     * E.g.: For base = "dir" and file = "dir/subdir/subsubdir" returns 2.
     *
     * @param file a file or folder within the [base]
     * @param base a File denoting a folder
     * @return number denoting how deep within the [base] folder the [file] is located.
     */
    fun getFileLevel(file: File, base: File): Int {
        if (file == base) return 0
        else if (!file.toPath().isDescendantOf(base.toPath())) error("$file is not a descendant file of $base")
        if (!base.isDirectory) error("$base is not a directory")
        var tmp: File? = file.relativeTo(base)
        var l = 0
        while (tmp != null) {
            tmp = tmp.parentFile
            l++
        }
        return l
    }

    fun isSeminarGroupFile(file: File): Boolean {
        val workDir = getWorkingDirectory()
            ?: error("Cannot test if file is a seminar group folder because the working directory is not set (null).")
        return file.isDirectory
                && (file.toPath().isDescendantOf(workDir)
                && getFileLevel(file, workDir.toFile()) == SEMINAR_GROUP_LEVEL)
    }

    fun isTutorFile(file: File): Boolean {
        val workDir = getWorkingDirectory()
            ?: error("Cannot test if file is a seminar group folder because the working directory is not set (null).")
        return file.isDirectory
                && (file.toPath().isDescendantOf(workDir)
                && getFileLevel(file, workDir.toFile()) == TUTOR_LEVEL)
    }

    fun isYearFile(file: File): Boolean {
        val workDir = getWorkingDirectory()
            ?: error("Cannot test if file is a seminar group folder because the working directory is not set (null).")
        return file.isDirectory
                && (file.toPath().isDescendantOf(workDir)
                && getFileLevel(file, workDir.toFile()) == YEAR_LEVEL)
    }

    fun getPresentTeams(folder: File?): List<MatchedNumberedFolder> {
        return getPresent(folder, this::matchesTeam)
    }

    fun getPresentWeeks(folder: File?): List<MatchedNumberedFolder> {
        return getPresent(folder, this::matchesWeek)
    }

    private fun getPresent(folder: File?, matches: KFunction1<String, Boolean>): List<MatchedNumberedFolder> {
        if (folder == null || !folder.isDirectory) return listOf()
        val result = mutableMapOf<Int, MatchedNumberedFolder>()
        folder.walk(FileWalkDirection.TOP_DOWN)
            .filter { it.isDirectory }
            .forEach { dir ->
                if (matches(dir.name)) {
                    val teamNumber = getNumberFromString(dir.name)
                    if (result.containsKey(teamNumber)) {
                        result[teamNumber]?.files?.add(dir)
                    } else {
                        result[teamNumber] = MatchedNumberedFolder(teamNumber, mutableSetOf(dir))
                    }
                }
            }
        return result.map { it.value }
    }

    private fun getNumberFromString(string: String): Int {
        val numberStr = string.replace(Regex("[^0-9]"), "").replaceFirst(Regex("^0+(?!$)"), "")
        return Integer.parseInt(numberStr)
    }

    fun getWeek(path: String): Int? {
        val match = REGEX_WEEK_FOLDER.find(path, 0)
        return if (match == null) null else getNumberFromString(match.value)
    }

    fun getTeam(path: String): Int? {
        val match = REGEX_TEAM_FOLDER.find(path, 0)
        return if (match == null) null else getNumberFromString(match.value)
    }

    private fun stringContainsWeek(weekNumber: Int, string: String): Boolean {
        val match = REGEX_WEEK_FOLDER.find(string, 0)
        return match !== null && getNumberFromString(match.value) == weekNumber
    }

    private fun stringContainsTeam(teamNumber: Int, string: String): Boolean {
        val match = REGEX_TEAM_FOLDER.find(string, 0)
        return match !== null && getNumberFromString(match.value) == teamNumber
    }

    private fun isPdfForWeekAndTeam(file: File, weekNumber: Int, team: Team, workDir: File?): Boolean {
        val relativePathString = if (workDir !== null) file.toRelativeString(workDir) else file.canonicalPath
        return isPdf(file)
                && stringContainsTeam(team.number, relativePathString)
                && stringContainsWeek(weekNumber, relativePathString)
    }

    fun getPdfs(week: Int, team: Team): List<Path> {
        val workDir = getWorkingDirectory()?.toFile()
        return team.seminarGroupFolder.walk()
            .filter { file -> isPdfForWeekAndTeam(file, week, team, workDir) }
            .map { it.toPath() }.toList()
    }

    fun getPdfs(week: Int, seminarGroup: File): List<Path> {
        val workDir = getWorkingDirectory()?.toFile()
        return seminarGroup.walk()
            .filter { file ->
                val relativePathString = if (workDir !== null) file.toRelativeString(workDir) else file.canonicalPath
                isPdf(file) && stringContainsWeek(week, relativePathString)
            }
            .map { it.toPath() }
            .toList()
    }
}

enum class FolderMessageType {
    SUCCESS, WARNING
}

data class FolderNameMessage(
    val message: String,
    val type: FolderMessageType,
)

open class MatchedNumberedFolder(
    open val number: Int,
    open val files: MutableSet<File> = mutableSetOf()
)
