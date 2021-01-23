package org.vpreportcorrector.utils

import org.apache.commons.io.FilenameUtils
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
