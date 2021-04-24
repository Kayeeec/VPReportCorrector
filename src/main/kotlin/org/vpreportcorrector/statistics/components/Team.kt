package org.vpreportcorrector.statistics.components

import java.io.File


open class Team(
    val seminarGroupFolder: File,
    override val number: Int = 0,
    override val files: MutableSet<File>): SeminarSubfolder(seminarGroupFolder, number, files) {

    init {
        foldersString = getFolderNamesString()
    }

    override fun toString(): String {
        return "Team $number $foldersString"
    }

    val longName: String
        get() {
            val tutorDir = seminarGroupFolder.parentFile
            val yearDirName = tutorDir.parentFile
            return "Team $number (${yearDirName.name}/${tutorDir.name}/${seminarGroupFolder.name})"
        }

    val name: String
        get() {
            return "Team $number"
        }
}

