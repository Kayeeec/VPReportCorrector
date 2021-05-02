package org.vpreportcorrector.diagram

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import org.icepdf.core.pobjects.annotations.Annotation
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.DiagramToggleEditModeEvent
import org.vpreportcorrector.app.DiagramSavedEvent
import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import org.vpreportcorrector.enums.DiagramIssue
import org.vpreportcorrector.utils.customButtonType
import tornadofx.*
import javax.swing.JComponent
import javax.swing.SwingUtilities

class DiagramViewModel(diagramModel: DiagramModel): ItemViewModel<DiagramModel>(diagramModel),
    WithLoading by LoadingLatch() {
    val diagramIssuesProperty = bind { diagramModel.diagramIssuesProperty }

    val isEditingProperty = SimpleBooleanProperty(false)
    val editToggleBtnLabel = stringBinding(this, isEditingProperty) {
        if (isEditingProperty.value)
            "View"
        else
            "Edit"
    }
    val editToggleBtnGraphic = objectBinding(this, isEditingProperty) {
        if (isEditingProperty.value)
            FontIcon(FontAwesomeSolid.EYE)
        else
            FontIcon(FontAwesomeSolid.EDIT)
    }
    val editToggleBtnTooltip = stringBinding(this, isEditingProperty) {
        if (isEditingProperty.value == true)
            "Save and switch to view mode"
        else
            "Switch to edit mode"
    }
    var viewerPanel: JComponent by singleAssign()
    var oldAnnotations: Set<String>? = null

    private val saveButtonType = ButtonType("Save and switch to view mode", ButtonBar.ButtonData.OK_DONE)
    private val justSwitchButtonType = ButtonType("Just switch to view mode", ButtonBar.ButtonData.NEXT_FORWARD)

    init {
        loadData()
    }

    private fun loadData() {
        startLoading()
        runAsync {
            item.loadDiagramErrors()
        } finally {
            endLoading()
        }
    }

    fun updateDiagramIssue(issue: DiagramIssue, isSelected: Boolean) {
        if (isSelected) {
            diagramIssuesProperty.add(issue)
        } else {
            diagramIssuesProperty.remove(issue)
        }
    }

    // todo - fix - always true, even if no changes
    fun hasUnsavedChanges(): Boolean {
        if (!isEditingProperty.value) return false
        return false && item.isModified() || isPdfModified()
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
                       customButtonType.DISCARD_CLOSE -> doClose = true
                   }
                },
                buttons = arrayOf(ButtonType.CANCEL, customButtonType.DISCARD_CLOSE)
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
            startLoading()
            item.savePdfDocument()
            item.saveAsJsonToFile()
        } success {
            fire(DiagramSavedEvent(path = item.path))
        } finally {
            endLoading()
        }
    }

    fun openDocument(actionFn: () -> Unit = {}) {
        SwingUtilities.invokeAndWait {
            item.swingController.openDocument(item.path.toAbsolutePath().toString())
            setAnnotationsReadAndLockedFlags(true)
            viewerPanel.revalidate()
        }
        actionFn()
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

    fun toggleViewEditMode() {
        val isEdit = !isEditingProperty.value
        isEditingProperty.value = isEdit
        if (!isEdit && hasUnsavedChanges()) {
            confirmation(
                title = "Unsaved changes",
                header = "There are unsaved changes, do you want to save them while switching to view mode?",
                actionFn = { buttonType: ButtonType ->
                    when(buttonType) {
                        saveButtonType -> { save() }
                    }
                },
                buttons = arrayOf(saveButtonType, justSwitchButtonType)
            )
        }
        if (!isEdit) { // View -> Edit, for now because detecting changes does not work correctly
            save()
        }

        SwingUtilities.invokeLater {
            setAnnotationsReadAndLockedFlags(!isEdit)
            viewerPanel.revalidate()
            oldAnnotations = getAllAnnotationsCopy()
        }
        fire(DiagramToggleEditModeEvent(item.path, isEdit))
    }
}
