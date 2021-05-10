package org.umlreviewer.statistics.components

import javafx.beans.property.SimpleSetProperty
import javafx.scene.control.CheckBox
import javafx.scene.control.TreeItem
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.components.expandAll
import org.umlreviewer.components.getTreeTopBar
import org.umlreviewer.statistics.components.ComponentHelpers.populateSeminarGroupTree
import org.umlreviewer.utils.FileTreeHelpers.isSeminarGroupFile
import org.umlreviewer.utils.getWorkingDirectory
import tornadofx.*
import java.io.File
import java.nio.file.Path

class SeminarGroupMultiSelect : Fragment(), RefreshableInputComponent {
    val selectedSeminarGroups: SimpleSetProperty<File> by param()
    private val checkboxes = mutableMapOf<File, CheckBox>()
    private val workdir = getWorkingDirectory()
    private var tree = getTree()

    private fun getTree() = treeview<Path?>(if (workdir != null) TreeItem(workdir) else null) {
            cellFormat {
                val file = it?.toFile()
                when {
                    it == null -> {
                        text = "No working directory set."
                    }
                    isSeminarGroupFile(file!!) -> {
                        val checkbox = checkbox {
                            text = file.name
                            isSelected = selectedSeminarGroups.contains(file)
                            action {
                                if (isSelected) selectedSeminarGroups.add(file)
                                else selectedSeminarGroups.remove(file)
                            }
                        }
                        graphic = checkbox
                        checkboxes[file] = checkbox
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
            fitToParentSize()
        }

    override val root = borderpane {
        fitToParentSize()
        top = getTreeTopBar(tree, "Select seminar groups:")
        center = tree
    }

    override fun refreshInputs() {
        checkboxes.forEach { (file, checkBox) ->
            checkBox.isSelected = selectedSeminarGroups.contains(file)
        }
    }
}
