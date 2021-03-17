package org.vpreportcorrector.mainview.content

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.Tooltip
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.DiagramSavedEvent
import org.vpreportcorrector.app.EditDiagramEvent
import org.vpreportcorrector.app.Styles.Companion.centered
import org.vpreportcorrector.app.Styles.Companion.textMuted
import org.vpreportcorrector.app.ViewDiagramEvent
import org.vpreportcorrector.diagram.DiagramController
import org.vpreportcorrector.diagram.DiagramModel
import org.vpreportcorrector.diagram.edit.DiagramEditorView
import org.vpreportcorrector.diagram.view.DiagramViewerView
import org.vpreportcorrector.utils.p
import tornadofx.*
import java.nio.file.Path

class ContentView : View("Content") {
    private val vm: ContentViewModel by inject()
    private var parentTabPane: TabPane by singleAssign()
    private val tabsNotEmpty = SimpleBooleanProperty(false)

    private val viewTabGraphic = FontIcon(FontAwesomeRegular.EYE)
    private val editTabGraphic = FontIcon(FontAwesomeRegular.EDIT)

    init {
        subscribe<ViewDiagramEvent> { event ->
            vm.checkToOpenOrActivate(event.path) { openTab(event.path, false) }
        }
        subscribe<EditDiagramEvent> { event ->
            vm.checkToOpenOrActivate(event.path) { openTab(event.path, true) }
        }
        subscribe<DiagramSavedEvent> { event ->
            vm.setTabSaved(event.path)
        }
    }

    private fun openTab(path: Path, isEditing: Boolean) {
        val diagramScope = Scope()
        val model = DiagramModel(path, isEditing)
        setInScope(model, diagramScope)
        val view = if (isEditing) find<DiagramEditorView>(diagramScope) else find<DiagramViewerView>(diagramScope)
        val diagramCtrl = find<DiagramController>(diagramScope)
        val tab = createTab(view, path, diagramCtrl)
        tab.graphic = if (isEditing) editTabGraphic else viewTabGraphic
        parentTabPane.tabs.add(tab)
        parentTabPane.selectionModel.select(tab)
        vm.openTabs[path] = TabData(tab, diagramCtrl)
    }

    private fun createTab(view: View, path: Path, diagramCtrl: DiagramController): Tab {
        val tab = Tab(path.toFile().name, vbox {
            fitToParentSize()
            add(view)
        })
        tab.tooltip = Tooltip(path.toAbsolutePath().toString())
        tab.id = path.toAbsolutePath().toString()
        tab.onCloseRequest = EventHandler { event ->
            if (!diagramCtrl.onClose()){
                tab.select()
                event.consume()
            } else {
                tab.close()
                vm.openTabs.remove(path)
            }
        }
        return tab
    }

    override val root = stackpane {
        fitToParentSize()
        vbox {
            fitToParentSize()
            hiddenWhen { tabsNotEmpty }
            alignment = Pos.CENTER
            p("No diagrams open") {
                addClass(textMuted, centered)
            }
        }
        parentTabPane = tabpane {
            fitToParentSize()
            tabs.onChange {
                tabsNotEmpty.value = parentTabPane.tabs.isNotEmpty()
            }
        }
    }
}

