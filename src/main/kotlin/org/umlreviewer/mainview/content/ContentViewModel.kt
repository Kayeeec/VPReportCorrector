package org.umlreviewer.mainview.content

import javafx.scene.control.ButtonType
import javafx.stage.Stage
import tornadofx.*
import java.nio.file.Path

class ContentViewModel: ViewModel() {
    val allOpenTabs = mutableMapOf<Path, TabData>().asObservable()
    val detachedWindowView: DetachedWindowView by inject()
    var detachedWindowStage: Stage? = null

    /**
     * Determines if any of the opened tabs have unsaved changes. Also marks all unsaved tabs.
     * @return true if at least one tab has unsaved changes, false otherwise
     */
    private fun hasTabsWithUnsavedChanges(): Boolean {
        val unsavedTabs = allOpenTabs.filter { it.value.viewModel.hasUnsavedChanges() }
        unsavedTabs.forEach {
            setTabUnsaved(it.key)
        }
        return unsavedTabs.isNotEmpty()
    }

    private fun setTabUnsaved(path: Path) {
        allOpenTabs[path]?.tab?.text = "*${path.toFile().name}"
    }

    fun setTabSaved(path: Path) {
        allOpenTabs[path]?.tab?.text = path.toFile().name
    }

    fun checkUnsavedChanges(): Boolean {
        var doClose = true
        if (hasTabsWithUnsavedChanges()) {
            confirmation(
                title = "Unsaved changes",
                header = "There are tabs with unsaved changes. Do you want to discard the changes and exit application?",
                actionFn = { buttonType: ButtonType ->
                    when(buttonType) {
                        ButtonType.CANCEL -> doClose = false
                        ButtonType.YES -> doClose = true
                    }
                },
                buttons = arrayOf(ButtonType.CANCEL, ButtonType.YES)
            )
        }
        return doClose
    }

    /**
     * Checks if there are any tabs with unsaved changes in the detached window,
     * marks the unsaved tabs and asks the user if the changes should be discarded.
     * If yes, disposes the tabs.
     * @return true if tabs have been disposed and closed and detached window can be closed, false otherwise
     */
    fun disposeDetachedTabsAndClose(): Boolean {
        var doClose = true
        val unsavedTabs = allOpenTabs.filter { it.value.viewModel.hasUnsavedChanges() }
        unsavedTabs.forEach {
            setTabUnsaved(it.key)
        }
        if (unsavedTabs.isNotEmpty()) {
            confirmation(
                title = "Unsaved changes",
                header = "There are tabs with unsaved changes. Do you want to discard the changes and close this window?",
                actionFn = { buttonType: ButtonType ->
                    when(buttonType) {
                        ButtonType.CANCEL -> doClose = false
                        ButtonType.YES -> doClose = true
                    }
                },
                buttons = arrayOf(ButtonType.CANCEL, ButtonType.YES)
            )
        }
        if (doClose) {
            val detachedTabs = allOpenTabs.filter { it.value.location == TabLocation.DETACHED_WINDOW }
            detachedTabs.forEach {
                it.value.viewModel.dispose()
                it.value.tab.close()
                allOpenTabs.remove(it.key)
            }
        }
        return doClose
    }

    fun checkToOpenOrActivate(path: Path, openFn: () -> Unit) {
        allOpenTabs.containsKey(path)
        if (allOpenTabs[path] == null) {
            openFn()
        } else {
            val tab = allOpenTabs[path]?.tab
            tab?.select()
            if (tab?.tabPane?.id == TabLocation.DETACHED_WINDOW.name) {
                detachedWindowStage?.toFront()
            }
        }
    }
}
