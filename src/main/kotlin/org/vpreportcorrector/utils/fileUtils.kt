package org.vpreportcorrector.utils

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
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

/**
 * Extension function to return the actual children.
 */
fun Path.list(): List<Path> = Files.list(this).use {
    it.toList().filter { path: Path ->
        !Files.isHidden(path) && Files.isReadable(path) && Files.isWritable(path)
    }
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
