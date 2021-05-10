package org.umlreviewer.statistics.components

import javafx.beans.property.SimpleSetProperty
import javafx.scene.control.CheckBox
import javafx.scene.control.TreeItem
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.components.expandAll
import org.umlreviewer.components.getTreeTopBar
import org.umlreviewer.statistics.components.ComponentHelpers.populateTeamTree
import org.umlreviewer.utils.file.Team
import org.umlreviewer.utils.getWorkingDirectory
import tornadofx.*
import java.nio.file.Path

class TeamMultiSelect : Fragment(), RefreshableInputComponent {
    val selectedTeams: SimpleSetProperty<Team> by param()
    private val workdir = getWorkingDirectory()
    private val checkboxes = mutableMapOf<Team, CheckBox>()

    private val tree = treeview<Any?>(TreeItem(workdir)) {
        cellFormat {
            when (it) {
                null -> {
                    text = "No working directory selected."
                }
                is Team -> {
                    val checkbox = checkbox {
                        text = it.toString()
                        isSelected = selectedTeams.contains(it)
                        action {
                            if (isSelected) selectedTeams.add(it)
                            else selectedTeams.remove(it)
                        }
                    }
                    graphic = checkbox
                    checkboxes[it] = checkbox
                }
                is Path -> {
                    graphic = FontIcon(FontAwesomeSolid.FOLDER)
                    text = it.toFile().name

                }
            }
        }
        populate { treeItem ->
            populateTeamTree(treeItem, workdir!!)
        }
        expandAll()
    }

    override val root = borderpane {
        top = getTreeTopBar(tree, "Select teams:")
        center = tree
    }

    override fun refreshInputs() {
        checkboxes.forEach { (team, checkBox) ->
            checkBox.isSelected = selectedTeams.contains(team)
        }
    }
}

