package org.vpreportcorrector.diagram

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.ButtonType
import org.icepdf.core.pobjects.annotations.Annotation
import org.vpreportcorrector.app.DiagramInEditModeEvent
import org.vpreportcorrector.app.DiagramSavedEvent
import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.diagram.enums.DiagramIssue
import tornadofx.*
import javax.swing.JComponent
import javax.swing.SwingUtilities

class DiagramViewModel(diagramModel: DiagramModel): ItemViewModel<DiagramModel>(diagramModel) {
    val diagramIssuesProperty = bind { diagramModel.diagramIssuesProperty }

    val isEditingProperty = SimpleBooleanProperty(false)
    var viewerPanel: JComponent by singleAssign()
    var loadingLatch = LoadingLatch()
    var oldAnnotations: Set<String>? = null

    init {
        loadData()
    }

    private fun loadData() {
        loadingLatch.startLoading()
        runAsync {
            item.loadDiagramErrors()
        } finally {
            loadingLatch.endLoading()
        }
    }

    fun updateDiagramIssue(issue: DiagramIssue, isSelected: Boolean) {
        if (isSelected) {
            diagramIssuesProperty.add(issue)
        } else {
            diagramIssuesProperty.remove(issue)
        }
    }

    fun hasUnsavedChanges(): Boolean {
        if (!isEditingProperty.value) return false
        return item.isModified() || isPdfModified()
    }

    private fun isPdfModified(): Boolean {
        return oldAnnotations != getAllAnnotationsCopy()
    }

    /**
     * Handles diagram tab closing. Checks if changes need to be saved.
     * @return true if the tab can be closed, false otherwise
     */
    fun onClose(): Boolean {
        if (!isEditingProperty.value) return true
        var doClose = true
        if (hasUnsavedChanges()) {
            confirmation(
                title = "Unsaved changes",
                header = "There are unsaved changes, do you want to discard them?",
                actionFn = { buttonType: ButtonType ->
                   when(buttonType) {
                       ButtonType.CANCEL -> doClose = false
                       ButtonType.YES -> {
                           doClose = true
                       }
                   }
                },
                buttons = arrayOf(ButtonType.CANCEL, ButtonType.YES)
            )
        }
        if (doClose) {
            dispose()
        }
        return doClose
    }

    fun dispose() {
        SwingUtilities.invokeAndWait {
            item.swingController.dispose()
        }
    }

    fun save() {
        runAsync {
            loadingLatch.startLoading()
            item.savePdfDocument()
            item.saveAsJsonToFile()
        } success {
            fire(DiagramSavedEvent(path = item.path))
        } finally {
            loadingLatch.endLoading()
        }
    }

    fun openDocument() {
        SwingUtilities.invokeLater {
            item.swingController.openDocument(item.path.toAbsolutePath().toString())
            setAnnotationsReadAndLockedFlags(true)
            viewerPanel.revalidate()
        }
    }

    private fun getAllAnnotationsCopy(): Set<String> {
        val result = mutableSetOf<String>()
        val pageTree = item.swingController.document.pageTree
        val n = pageTree.numberOfPages
        for (i in 0..n) {
            val page = pageTree.getPage(i)
            page?.annotations?.forEach { annotation: Annotation? ->
                annotation?.let {
                    result.add(it.toString())
                }
            }
        }
        return result.toSet()
    }

    private fun setAnnotationsReadAndLockedFlags(value: Boolean) {
        val pageTree = item.swingController.document.pageTree
        val n = pageTree.numberOfPages
        for (i in 0..n) {
            val page = pageTree.getPage(i)
            page?.annotations?.forEach {
                it?.setFlag(Annotation.FLAG_READ_ONLY, value)
                it?.setFlag(Annotation.FLAG_LOCKED_CONTENTS, value)
                it?.setFlag(Annotation.FLAG_LOCKED, value)
            }
        }
    }

    fun switchToEditMode() {
        isEditingProperty.value = true
        SwingUtilities.invokeLater {
            setAnnotationsReadAndLockedFlags(false)
            viewerPanel.revalidate()
            oldAnnotations = getAllAnnotationsCopy()
        }
        fire(DiagramInEditModeEvent(item.path))
    }
}
