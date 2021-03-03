package org.vpreportcorrector.mainview

import com.panemu.tiwulfx.control.dock.DetachableTab
import com.panemu.tiwulfx.control.dock.DetachableTabPane
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
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
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths

// TODO: 03.03.21 refactor to MVC
class ContentView : View("Content") {

    private var parentTabPane = DetachableTabPane()
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
            setDetachabilities()
        }
    }

    private fun openInEditTab(path: Path) {
        val editScope = Scope()
        val model = DiagramModel(path, true)
        setInScope(model, editScope)
        val editorView = find<DiagramEditorView>(editScope)
        val tab = getDetachableTab(editorView, path, editScope)
        tab.graphic = FontIcon(editTabGraphic)
        parentTabPane.tabs.add(tab)
        openTabs[path] = TabData(tab, model)
    }

    private fun openInViewTab(path: Path) {
        val viewScope = Scope()
        val model = DiagramModel(path, false)
        setInScope(model, viewScope)
        val viewerView = find<DiagramViewerView>(viewScope)
        val tab = getDetachableTab(viewerView, path, viewScope)
        tab.graphic = FontIcon(viewTabGraphic)
        parentTabPane.tabs.add(tab)
        openTabs[path] = TabData(tab, model)
    }

    private fun getDetachableTab(view: View, path: Path, viewScope: Scope): DetachableTab {
        val tab = DetachableTab(path.toFile().name, vbox {
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

    private fun setDetachabilities(){
        if (parentTabPane.tabs.size == 1) {
            (parentTabPane.tabs.first() as DetachableTab).isDetachable = false
        } else {
            parentTabPane.tabs.forEach { tab ->
                (tab as DetachableTab).isDetachable = true
            }
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

        parentTabPane.setStageOwnerFactory { stage ->
            stage.title = t("appName")
            stage.onCloseRequest = EventHandler { e ->
                // TODO: 03.03.21 closing detached tabs might need further testing when the editor and unsaved changes
                //  dialogs are implemented
                val tabPane = stage.scene.root as DetachableTabPane
                val tabs = tabPane.tabs.map { it as DetachableTab }
                val unsavedTabs = filterTabsWithUnsavedChanges(tabs)
                if (unsavedTabs.isNotEmpty()){
                    e.consume()
                    unsavedTabs.first().select()
                    // TODO: 03.03.21 unsaved changes alert and close anyway discarting changes logic
                } else {
                    tabs.forEach {
                        it.requestClose()
                    }
                }
            }
            scene.window
        }
        parentTabPane.setSceneFactory { tabPane ->
            val newScene = Scene(tabPane, 900.0, 800.0)
            newScene
        }
        add(parentTabPane)
        parentTabPane.fitToParentSize()
    }

    /**
     * Might be useful later for saving/initializing last opened tabs.
     */
    private fun DetachableTab.tabType(): TabType? {
        return when ((this.graphic as FontIcon).iconCode) {
            editTabGraphic -> TabType.EDIT
            viewTabGraphic -> TabType.VIEW
            else -> null
        }
    }

    /**
     * Fires the closing event on a tab, so that its onCloseRequest handler is executed.
     * Needed for proper unsaved changes handling and tracking of opened tabs.
     */
    private fun DetachableTab.requestClose() {
        Event.fireEvent(this, Event(DetachableTab.TAB_CLOSE_REQUEST_EVENT))
    }

    private data class TabData(val tab: DetachableTab, val model: DiagramModel)

    /**
     * Searches a collection of tabs for those with unsaved changes.
     * @param tabs - a collection of tabs to search in
     * @return a list of tabs with unsaved changes
     */
    fun filterTabsWithUnsavedChanges(tabs: Collection<DetachableTab>): List<DetachableTab> {
        val result = mutableListOf<DetachableTab>()
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
