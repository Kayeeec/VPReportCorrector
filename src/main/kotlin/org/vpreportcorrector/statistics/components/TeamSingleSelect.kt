package org.vpreportcorrector.statistics.components

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ToggleGroup
import javafx.scene.control.TreeItem
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.components.NoSelectionModel
import org.vpreportcorrector.components.expandAll
import org.vpreportcorrector.components.getTreeTopBar
import org.vpreportcorrector.statistics.components.ComponentHelpers.populateTeamTree
import org.vpreportcorrector.utils.getWorkingDirectory
import tornadofx.*
import java.nio.file.Path

class TeamSingleSelect : Fragment() {
    val selectedTeam: SimpleObjectProperty<Team> by param()

    private val workdir = getWorkingDirectory()
    private val radioToggleGroup = ToggleGroup()

    private val tree = treeview<Any?>(TreeItem(workdir)) {
        selectionModel = NoSelectionModel()
        cellFormat {
            when (it) {
                null -> {
                    text = "No working directory selected."
                }
                is Team -> {
                    graphic = radiobutton(text = it.toString(), group = radioToggleGroup, value = it) {
                        isSelected = selectedTeam.value == it
                        action { isSelected = true }
                    }
                }
                is Path -> {
                    graphic = FontIcon(FontAwesomeSolid.FOLDER)
                    text = it.toFile().name
                }
            }
        }
        populate { treeItem -> populateTeamTree(treeItem, workdir!!) }
        expandAll()
    }

    init {
        radioToggleGroup.selectedValueProperty<Team>().bindBidirectional(selectedTeam)
    }

    override val root = borderpane {
        top = getTreeTopBar(tree, "Select team:")
        center = tree
    }
}
