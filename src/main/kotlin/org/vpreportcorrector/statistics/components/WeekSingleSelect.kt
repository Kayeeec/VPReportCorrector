package org.vpreportcorrector.statistics.components

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.scene.control.ToggleGroup
import javafx.scene.control.TreeItem
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.components.NoSelectionModel
import org.vpreportcorrector.components.expandAll
import org.vpreportcorrector.components.getTreeTopBar
import org.vpreportcorrector.utils.FileTreeHelpers
import org.vpreportcorrector.utils.getWorkingDirectory
import org.vpreportcorrector.utils.list
import tornadofx.*
import java.nio.file.Path

class WeekSingleSelect : Fragment(), RefreshableInputComponent {
    val selectedWeek: SimpleObjectProperty<Week> by param()
    val labelText: String by param("Select week")

    private val workdir = getWorkingDirectory()
    private val tg = ToggleGroup()

    private val tree = treeview<Any?>(TreeItem(workdir)) {
        selectionModel = NoSelectionModel()
        cellFormat {
            when (it) {
                null -> {
                    text = "No working directory selected."
                }
                is Week -> {
                    graphic = radiobutton(text = it.toString(), tg, value = it) {
                        isSelected = selectedWeek.value == it
                        onMouseClicked = EventHandler { e -> // todo - why is action not working here and works fine in th TeamSingleSelect
                            selectedWeek.value = it
                            isSelected = true
                        }
                    }
                }
                is Path -> {
                    graphic = FontIcon(FontAwesomeSolid.FOLDER)
                    text = it.toFile().name
                }
            }
        }
        populate { treeItem ->
            val value = treeItem.value
            if (value == null)
                null
            else if (value is Path && FileTreeHelpers.getFileLevel(value.toFile(), workdir!!.toFile()) < FileTreeHelpers.YEAR_LEVEL)
                    value.list().filter { it.toFile().isDirectory }
            else if (value is Path && FileTreeHelpers.isYearFile(value.toFile()))
                FileTreeHelpers.getPresentWeeks(value.toFile())
                    .map { Week(value.toFile(), it.number, it.files) }
                    .sortedBy { it.number }
            else
                null
        }
        expandAll()
    }

    init {
        tg.selectedValueProperty<Week>().bindBidirectional(selectedWeek)
    }

    override val root = borderpane {
        top = getTreeTopBar(tree, labelText)
        center = tree
    }

    override fun refreshInputs() {
        // does not have to be cleared thanks to bidirectionally bound toggle group
    }
}
