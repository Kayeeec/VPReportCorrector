package org.umlreviewer.statistics.components

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ToggleGroup
import javafx.scene.control.TreeItem
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.components.NoSelectionModel
import org.umlreviewer.components.expandAll
import org.umlreviewer.components.getTreeTopBar
import org.umlreviewer.statistics.components.ComponentHelpers.populateSeminarGroupTree
import org.umlreviewer.utils.file.FileTreeHelpers.isSeminarGroupFile
import org.umlreviewer.utils.getWorkingDirectory
import tornadofx.*
import java.io.File
import java.nio.file.Path

class SeminarGroupSingleSelect : Fragment() {
    val selectedSeminarGroup: SimpleObjectProperty<File> by param()

    private val workdir = getWorkingDirectory()
    private val radioToggleGroup = ToggleGroup()

    private val tree = treeview(TreeItem(workdir)) {
        selectionModel = NoSelectionModel()
        cellFormat {
            val file = it?.toFile()
            when {
                it == null -> {
                    text = "No working directory set."
                }
                isSeminarGroupFile(file!!) -> {
                    graphic = getSelectableNode(file, it)
                }
                else -> {
                    graphic = FontIcon(FontAwesomeSolid.FOLDER)
                    text = file.name
                }
            }
        }
        populate { parent ->
            populateSeminarGroupTree(parent, workdir!!)
        }
        expandAll()
    }

    private fun getSelectableNode(file: File, it: Path) =
        radiobutton(text = file.name, group = radioToggleGroup, value = file) {
            isSelected = selectedSeminarGroup.value == it.toFile()
            action { isSelected = true }
        }

    init {
        radioToggleGroup.selectedValueProperty<File>().bindBidirectional(selectedSeminarGroup)
    }

    override val root = borderpane {
        top = getTreeTopBar(tree, "Select seminar group:")
        center = tree
    }
}

