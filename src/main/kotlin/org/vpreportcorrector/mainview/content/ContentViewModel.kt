package org.vpreportcorrector.mainview.content

import javafx.scene.control.ButtonType
import tornadofx.ViewModel
import tornadofx.asObservable
import tornadofx.confirmation
import tornadofx.select
import java.nio.file.Path

class ContentViewModel: ViewModel() {
    val openTabs = mutableMapOf<Path, TabData>().asObservable()

    /**
     * Determines if any of the opened tabs have unsaved changes. Also marks all unsaved tabs.
     * @return true if at least one tab has unsaved changes, false otherwise
     */
    private fun hasTabsWithUnsavedChanges(): Boolean {
        val unsavedTabs = openTabs.filter { it.value.controller.hasUnsavedChanges() }
        unsavedTabs.forEach {
            setTabUnsaved(it.key)
        }
        return unsavedTabs.isNotEmpty()
    }

    private fun setTabUnsaved(path: Path) {
        openTabs[path]?.tab?.text = "*${path.toFile().name}"
    }

    fun setTabSaved(path: Path) {
        openTabs[path]?.tab?.text = path.toFile().name
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

    fun checkToOpenOrActivate(path: Path, openFn: () -> Unit) {
        openTabs.containsKey(path)
        if (openTabs[path] == null) {
            openFn()
        } else {
            openTabs[path]?.tab?.select()
        }
    }
}
