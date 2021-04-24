package org.vpreportcorrector.statistics.components

import java.io.File

abstract class SeminarSubfolder(
    open val parent: File,
    open val number: Int,
    open val files: MutableSet<File>,
) {
    protected var foldersString: String = ""

    protected fun getFolderNamesString(): String {
        val folderNames = files.map { it.name }.toSet()
        return if (folderNames.isNotEmpty())
            "(\"${folderNames.joinToString("\", \"")}\")"
        else ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SeminarSubfolder

        if (parent != other.parent) return false
        if (number != other.number) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parent.hashCode()
        result = 31 * result + number
        return result
    }

}
