package org.umlreviewer.mainview.content

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Tab
import javafx.scene.control.Tooltip
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.DiagramToggleEditModeEvent
import org.umlreviewer.DiagramSavedEvent
import org.umlreviewer.OpenDiagramEvent
import org.umlreviewer.OpenDiagramInNewWindowEvent
import org.umlreviewer.styles.Styles.Companion.centered
import org.umlreviewer.styles.Styles.Companion.textMuted
import org.umlreviewer.diagram.DiagramModel
import org.umlreviewer.diagram.DiagramView
import org.umlreviewer.diagram.DiagramViewModel
import org.umlreviewer.utils.p
import tornadofx.*
import java.nio.file.Path

class ContentView : View("Content") {
    private val vm: ContentViewModel by inject()

    private var tabPane = tabpane {
        id = TabLocation.MAIN.name
        fitToParentSize()
    }

    init {
        subscribe<OpenDiagramInNewWindowEvent> { event ->
            vm.checkToOpenOrActivate(event.path) { openInNewWindow(event.path) }
        }
        subscribe<OpenDiagramEvent> { event ->
            vm.checkToOpenOrActivate(event.path) { openTab(event.path) }
        }
        subscribe<DiagramSavedEvent> { event ->
            vm.setTabSaved(event.path)
        }
        subscribe<DiagramToggleEditModeEvent> { event ->
            vm.allOpenTabs[event.path]?.tab?.let {
                it.graphic = if (event.isEditing) FontIcon(FontAwesomeRegular.EDIT)
                    else FontIcon(FontAwesomeRegular.EYE)
            }
        }
    }

    private fun openTab(path: Path) {
        val tab = createAndTrackTab(path, TabLocation.MAIN)
        tab.graphic = FontIcon(FontAwesomeRegular.EYE)
        tabPane.tabs.add(tab)
        tabPane.selectionModel.select(tab)
    }

    private fun openInNewWindow(path: Path) {
        val tab = createAndTrackTab(path, TabLocation.DETACHED_WINDOW)
        vm.detachedWindowView.tabPane.tabs.add(tab)
        tab.select()

        if (vm.detachedWindowStage == null || !vm.detachedWindowStage!!.isShowing) {
            vm.detachedWindowStage = vm.detachedWindowView.openWindow()
        }
        vm.detachedWindowStage?.toFront()
    }

    private fun createAndTrackTab(path: Path, location: TabLocation): Tab {
        val diagramScope = Scope()
        val dvm = DiagramViewModel(DiagramModel(path))
        setInScope(dvm, diagramScope)
        val view = find<DiagramView>(diagramScope)
        val diagramCtrl = find<DiagramViewModel>(diagramScope)

        val tab = Tab(path.toFile().name, view.root)
        tab.graphic = FontIcon(FontAwesomeRegular.EYE)
        tab.tooltip = Tooltip(path.toAbsolutePath().toString())
        tab.id = path.toAbsolutePath().toString()
        tab.onCloseRequest = EventHandler { event ->
            if (!diagramCtrl.onClose()){
                tab.select()
                event.consume()
            } else {
                tab.close()
                vm.allOpenTabs.remove(path)
            }
        }
        vm.allOpenTabs[path] = TabData(tab, diagramCtrl, location)
        return tab
    }

    override val root = stackpane {
        fitToParentSize()
        vbox {
            fitToParentSize()
            visibleWhen { Bindings.size(tabPane.tabs).eq(0) }
            alignment = Pos.CENTER
            p("No diagrams open") {
                addClass(textMuted, centered)
            }
        }
        add(tabPane)
    }
}
