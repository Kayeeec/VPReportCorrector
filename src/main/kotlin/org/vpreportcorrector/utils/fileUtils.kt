package org.vpreportcorrector.utils

import javafx.application.Platform
import javafx.stage.Modality
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.SystemUtils
import org.vpreportcorrector.app.errorhandling.ErrorCollector
import org.vpreportcorrector.filesexplorer.dialogs.*
import tornadofx.Scope
import tornadofx.find
import tornadofx.setInScope
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.FutureTask
import kotlin.streams.toList


fun suggestName(dest: Path, file: Path): String {
    val targetDestChildren = dest.list().map { it.toFile().name }.toSet()
    var i = 1
    val name = FilenameUtils.removeExtension(file.toFile().name)
    val noDotExt = FilenameUtils.getExtension(file.toFile().name)
    val ext = if (noDotExt.isBlank()) noDotExt else ".$noDotExt"
    var tmpName = "${name} (${i})$ext"
    while (targetDestChildren.contains(tmpName)) {
        i += 1
        tmpName = "$name ($i)$ext"
    }
    return tmpName
}

fun findConflictingFile(targetDir: Path, file: Path): Path? {
    if (!targetDir.toFile().isDirectory) return null
    val name = file.toFile().name
    return targetDir.list().find { it.toFile().name == name }
}

fun Path.isDescendantOf(possibleParent: Path): Boolean {
    if (!possibleParent.toFile().isDirectory) return false
    var parent: Path? = this.parent
    while (parent != null) {
        if (parent == possibleParent) return true
        parent = parent.parent
    }
    return false
}

fun File.isWithinOrEqual(possibleParentDir: File?): Boolean {
    if (possibleParentDir == null || !possibleParentDir.isDirectory) return false
    return this == possibleParentDir || this.toPath().isDescendantOf(possibleParentDir.toPath())
}

/**
 * Extension function to return the actual children of java.nio.file.Path.
 * Filters out hidden files.
 * Files are sorted first by extension (no extension = directory first) and then by name.
 */
fun Path.list(): List<Path> = Files.list(this).use { stream ->
    stream.toList().filter { path: Path ->
        !Files.isHidden(path) && Files.isReadable(path) && Files.isWritable(path)
    }.sortedWith(
        compareBy<Path>{ it.toFile().extension }.thenBy { it.fileName.toString() }
    )
}

fun getUserHomeDirectory(): File? {
    return try {
        SystemUtils.getUserHome()
    } catch (e: Exception){
        null
    }
}

val ILLEGAL_FILE_CHARACTERS = setOf('/', '\n', '\r', '\t', '\u0000', '\u000C', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
val ILLEGAL_FILE_NAMES = setOf(".", "..", "")
val ILLEGAL_FILE_NAMES_WINDOWS = setOf(
    *ILLEGAL_FILE_NAMES.toTypedArray(),
    "CON", "PRN", "AUX", "NUL",
    "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
    "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
)
private fun getIllegalNames(): Set<String> {
    return if (SystemUtils.IS_OS_WINDOWS) ILLEGAL_FILE_NAMES_WINDOWS else ILLEGAL_FILE_NAMES
}

/**
 * Crudely (!) checks if given string is a valid directory name by checking:
 *      - if it contains any illegal characters (also @see ILLEGAL_FILE_CHARACTERS constant),
 *      - if it is equal to any illegal names (also @see ILLEGAL_FILE_NAMES* constants, OS specific),
 *      - if it ends with '.',
 *
 *  Given string is trimmed for checking, because it is expected to be trimmed when creating a directory.
 *
 *  Illegal characters:
 *      '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'
 *  Illegal names:
 *      ".", "..", "",
 *  Additional illegal names for Windows:
 *      "CON", "PRN", "AUX", "NUL",
 *      "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
 *      "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
 *
 *  @param name     String, given name to be checked
 *  @return true if the name is "valid" (see above), false otherwise
 */
fun isValidFileName(name: String): Boolean {
    val trimmedName = name.trim()
    return ILLEGAL_FILE_CHARACTERS.none { trimmedName.contains(it) }
            && !getIllegalNames().contains(trimmedName)
            && !trimmedName.endsWith(".")
}

/**
 * Copying files
 */
fun copyFiles(targetDir: File, copiedFiles: List<Path>) {
    val errorCollector = ErrorCollector("Error/s occurred while copying:")
    var remembered = RememberChoice()
    copiedFiles.forEach { path ->
        try {
            remembered = checkConflictsAndCopyFileOrDir(remembered, path, targetDir)
        } catch (e: Exception) {
            errorCollector.addError(
                "Error occurred copying file ${path.toAbsolutePath()} to destination: ${targetDir.absolutePath}",
                e
            )
        }
    }
    Platform.runLater { errorCollector.verify() }
}

fun checkConflictsAndCopyFileOrDir(rememberedAction: RememberChoice, copied: Path, targetDir: File): RememberChoice {
    val (conflictingFile, result) = checkConflicts(targetDir, copied, rememberedAction)
    var remChoice = rememberedAction
    if (conflictingFile != null) {
        remChoice = resolveRememberedAction(rememberedAction, result, copied)
        val chosen = remChoice.getRelevantChoice(copied)
        if (chosen == FileConflictChoice.REPLACE_OR_MERGE
            || result?.choice == FileConflictChoice.REPLACE_OR_MERGE) {
            remChoice = replaceFileOrMergeDirectory(copied, targetDir, conflictingFile, remChoice)
        }
        else if (chosen == FileConflictChoice.RENAME || result?.choice == FileConflictChoice.RENAME){
            val newName = result?.newName ?: suggestName(targetDir.toPath(), copied)
            copyUsingNewName(copied, targetDir, newName)
        }
    } else {
        copyFileOrDirectory(copied, targetDir)
    }
    return remChoice
}

private fun replaceFileOrMergeDirectory(
    copied: Path,
    targetDir: File,
    conflictingFile: Path,
    rememberedAction: RememberChoice
): RememberChoice {
    var remAction = rememberedAction
    if (copied.toFile().isDirectory) {
        copied.list().forEach { path ->
            remAction = checkConflictsAndCopyFileOrDir(remAction, path, conflictingFile.toFile())
        }
    } else {
        if (!Files.isSameFile(copied, conflictingFile)) {
            FileUtils.copyFileToDirectory(copied.toFile(), targetDir)
        }
    }
    return remAction
}

/* non conflicting */
private fun copyFileOrDirectory(copied: Path, targetDir: File) {
    if (copied.toFile().isDirectory) {
        FileUtils.copyDirectoryToDirectory(copied.toFile(), targetDir)
    } else {
        FileUtils.copyFileToDirectory(copied.toFile(), targetDir)
    }
}

private fun copyUsingNewName(copied: Path, targetDir: File, newName: String) {
    if (copied.toFile().isDirectory) {
        val newDir = File(targetDir, newName)
        newDir.mkdirs()
        FileUtils.copyDirectory(copied.toFile(), newDir)
    } else {
        val newFile = File(targetDir, newName)
        FileUtils.copyFile(copied.toFile(), newFile)
    }
}

private fun resolveRememberedAction(
    rememberedAction: RememberChoice,
    result: FileConflictResult?,
    copied: Path,
): RememberChoice {
    if (copied.toFile().isDirectory) {
        if (rememberedAction.directory == null && result != null && result.remember) {
            rememberedAction.directory = result.choice
            return rememberedAction
        }
        return rememberedAction
    } else {
        if (rememberedAction.file == null && result != null && result.remember) {
            rememberedAction.file = result.choice
            return rememberedAction
        }
        return rememberedAction
    }
}

private fun checkConflicts(targetDir: File, path: Path, rememberedAction: RememberChoice): Pair<Path?, FileConflictResult?> {
    val conflictingFile = findConflictingFile(targetDir.toPath(), path)
    var result: FileConflictResult? = null

    if (conflictingFile != null && rememberedAction.getRelevantChoice(path) == null) {
        result = openFileExistsDialog(conflictingFile, path)
    }
    return Pair(conflictingFile, result)
}

fun openFileExistsDialog(existing: Path, copied: Path): FileConflictResult {
    val futureTask = FutureTask {
        val scope = Scope()
        val model = FileExistsDialogModel(FileExistsDialog(existing, copied))
        setInScope(model, scope)
        find<FileExistsDialogView>(scope).openModal(
            block = true,
            modality = Modality.WINDOW_MODAL,
            resizable = false,
            escapeClosesWindow = false,
        )
        FileConflictResult(model)
    }
    Platform.runLater(futureTask)
    return futureTask.get()
}

fun isPdf(file: File): Boolean {
    return file.extension.equals("pdf", true)
}

fun isImage(file: File): Boolean {
    return listOf("jpg", "png", "bmp", "jpeg", "svg").any { it.equals(file.extension, true) }
}

fun File.isWriteable() = if (this.exists()) this.canWrite() else this.parentFile.canWrite()
