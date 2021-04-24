package org.vpreportcorrector.statistics.components

import java.io.File

class Week(
    yearFolder: File,
    override val number: Int = 0,
    override val files: MutableSet<File>): SeminarSubfolder(yearFolder, number, files) {

    init {
        foldersString = getFolderNamesString()
    }

    override fun toString(): String {
        return "Week $number $foldersString"
    }

    val name: String
        get() = "Week $number"
}
