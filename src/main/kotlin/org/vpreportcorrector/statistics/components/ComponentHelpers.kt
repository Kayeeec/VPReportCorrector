package org.vpreportcorrector.statistics.components

import javafx.scene.control.TreeItem
import org.vpreportcorrector.utils.FileTreeHelpers
import org.vpreportcorrector.utils.list
import java.nio.file.Path

object ComponentHelpers {
    fun populateSeminarGroupTree(parent: TreeItem<Path?>, workdir: Path): Iterable<Path?>? {
        val value = parent.value
        return when {
            value == null -> null
            FileTreeHelpers.getFileLevel(value.toFile(), workdir.toFile()) < 3 -> value.list().filter { it.toFile().isDirectory }
            else -> null
        }
    }

    fun populateTeamTree(treeItem: TreeItem<Any?>, workdir: Path): Iterable<Any?>? {
        val value = treeItem.value
        return if (value == null) null
        else if (value is Path && FileTreeHelpers.getFileLevel(value.toFile(), workdir.toFile()) < 3)
            value.list().filter { it.toFile().isDirectory }
        else if (value is Path && FileTreeHelpers.isSeminarGroupFile(value.toFile()))
            FileTreeHelpers.getPresentTeams(value.toFile()).map {
                Team(value.toFile(), it.number, it.files)
            }
        else null
    }
}
