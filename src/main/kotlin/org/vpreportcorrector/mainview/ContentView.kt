package org.vpreportcorrector.mainview

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.control.Tooltip
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular
import org.kordamp.ikonli.javafx.FontIcon
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
import java.nio.file.Paths

// TODO: 03.03.21 refactor to MVC
class ContentView : View("Content") {

    private var parentTabPane = tabpane {
        fitToParentSize()
    }

    private val tabsNotEmpty = SimpleBooleanProperty(parentTabPane.tabs.isNotEmpty())

    private val openTabs = mutableMapOf<Path, TabData>()

    private val viewTabGraphic = FontAwesomeRegular.EYE
    private val editTabGraphic = FontAwesomeRegular.EDIT

    init {
        subscribe<ViewDiagramEvent> { event ->
            checkToOpenOrActivate(event.path) { openInViewTab(event.path) }
        }
        subscribe<EditDiagramEvent> { event ->
            checkToOpenOrActivate(event.path) { openInEditTab(event.path) }
        }
        parentTabPane.tabs.onChange {
            tabsNotEmpty.value = parentTabPane.tabs.isNotEmpty()
        }
    }

    private fun openInEditTab(path: Path) {
        val editScope = Scope()
        val model = DiagramModel(path, true)
        setInScope(model, editScope)
        val editorView = find<DiagramEditorView>(editScope)
        val tab = createTab(editorView, path, editScope)
        tab.graphic = FontIcon(editTabGraphic)
        parentTabPane.tabs.add(tab)
        openTabs[path] = TabData(tab, model)
    }

    private fun openInViewTab(path: Path) {
        val viewScope = Scope()
        val model = DiagramModel(path, false)
        setInScope(model, viewScope)
        val viewerView = find<DiagramViewerView>(viewScope)
        val tab = createTab(viewerView, path, viewScope)
        tab.graphic = FontIcon(viewTabGraphic)
        parentTabPane.tabs.add(tab)
        openTabs[path] = TabData(tab, model)
    }

    private fun createTab(view: View, path: Path, viewScope: Scope): Tab {
        val tab = Tab(path.toFile().name, vbox {
            fitToParentSize()
            add(view)
        })
        tab.tooltip = Tooltip(path.toAbsolutePath().toString())
        tab.id = path.toAbsolutePath().toString()
        tab.onCloseRequest = EventHandler { event ->
            val controller = find<DiagramController>(viewScope)
            if (!controller.onClose()){
                tab.select()
                event.consume()
            } else {
                tab.close()
                openTabs.remove(path)
            }
        }
        return tab
    }

    private fun checkToOpenOrActivate(path: Path, openFn: () -> Unit) {
        openTabs.containsKey(path)
        if (openTabs[path] == null) {
            openFn()
        } else {
            parentTabPane.selectionModel.select(openTabs[path]?.tab)
        }
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

        add(parentTabPane)

    }

    /**
     * Fires the closing event on a tab, so that its onCloseRequest handler is executed.
     * Needed for proper unsaved changes handling and tracking of opened tabs.
     */
    private fun Tab.requestClose() {
        Event.fireEvent(this, Event(Tab.TAB_CLOSE_REQUEST_EVENT))
    }

    private data class TabData(val tab: Tab, val model: DiagramModel)

    /**
     * Searches a collection of tabs for those with unsaved changes.
     * @param tabs - a collection of tabs to search in
     * @return a list of tabs with unsaved changes
     */
    fun filterTabsWithUnsavedChanges(tabs: Collection<Tab>): List<Tab> {
        val result = mutableListOf<Tab>()
        tabs.forEach { tab ->
            val path = Paths.get(tab.id)
            if (openTabs[path]?.model?.hasUnsavedChangesProperty?.value == true) {
                result.add(tab)
            }
        }
        return result.toList()
    }

    /**
     * Determines if any of the opened tabs have unsaved changes.
     * @return true if at least one tab has unsaved changes, false otherwise
     */
    fun hasTabsWithUnsavedChanges(): Boolean {
        return openTabs.any { it.value.model.hasUnsavedChangesProperty.value == true }
    }
}
