package org.vpreportcorrector.diagram

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.ButtonType
import org.icepdf.core.pobjects.annotations.Annotation
import org.icepdf.ri.common.SwingController
import org.icepdf.ri.common.ViewModel
import org.vpreportcorrector.app.DiagramSavedEvent
import tornadofx.*
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import javax.swing.JComponent
import javax.swing.SwingUtilities

class DiagramController: Controller() {
    val model: DiagramModel by inject()
    val swingController = SwingController()
    var viewerPanel: JComponent by singleAssign()

    fun loadData() {
        model.loadingLatch.startLoading()
        runAsync {
            // TODO: 21.02.21 will load error tags for given path from "repository"
            // mock data for now
            model.diagramErrorsProperty.clear()
            model.diagramErrorsProperty.addAll(getMockErrorTags())
        } finally {
            model.loadingLatch.endLoading()
        }
    }

    private fun getMockErrorTags(): MutableList<Pair<String, SimpleBooleanProperty>> { // TODO: 22.02.21 remove
        val result = mutableListOf<Pair<String, SimpleBooleanProperty>>()
        for (i in 1..25) {
            result.add(Pair("UML error $i", SimpleBooleanProperty((1..2).shuffled().first() % 2 == 0)))
        }
        result.add(Pair(
            "Very long uml error name lorem ipsum sit amet dolor color sum devon priga friga freza",
            SimpleBooleanProperty(false)
        ))
        return result
    }

    fun hasUnsavedChanges(): Boolean {
        // TODO: 16.03.21 add check for diagram errors changes - after MVVM refactor
        if (!model.isEditing) return false
        // TODO KB: bug icepdf? - even after reopening the document on save the state manager is non empty
        //  therefore it always looks unsaved, implement own annotations change test
//        return isPdfModified()
        return false
    }

    private fun isPdfModified(): Boolean {
        var isPdfModified = false
        SwingUtilities.invokeAndWait {
            isPdfModified = swingController.document.stateManager.isChanged
        }
        return isPdfModified
    }

    /**
     * Handles diagram tab closing. Checks if changes need to be saved.
     * @return true if the tab can be closed, false otherwise
     */
    fun onClose(): Boolean {
        if (!model.isEditing) return true
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
            SwingUtilities.invokeAndWait {
                swingController.dispose()
            }
        }
        return doClose
    }

    fun save() {
        runAsync {
            model.loadingLatch.startLoading()
            savePdfDocument()
            saveDiagramErrors()
        } success {
            fire(DiagramSavedEvent(path = model.path))
        } finally {
            model.loadingLatch.endLoading()
        }
    }

    private fun saveDiagramErrors() {
        // TODO: saveDiagramErrors - implemented when sync to disk and persistence method implemented
    }

    private fun savePdfDocument() {
        try {
            SwingUtilities.invokeAndWait {
                val file = model.path.toFile()
                val fileOutputStream = FileOutputStream(file)
                val buf = BufferedOutputStream(fileOutputStream, 8192)
                if (!swingController.document.stateManager.isChanged) {
                    swingController.document.writeToOutputStream(buf)
                } else {
                    swingController.document.saveToOutputStream(buf)
                }
                buf.flush()
                fileOutputStream.flush()
                buf.close()
                fileOutputStream.close()
                ViewModel.setDefaultFile(file)
            }
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
        }
    }

    /**
     * Must be run on swing thread!
     */
    fun openDocument() {
        SwingUtilities.invokeLater {
            swingController.openDocument(model.path.toAbsolutePath().toString())
            if (model.isEditing) {
                makeAnnotationsReadOnly()
            }
            viewerPanel.revalidate()
        }
    }

    private fun makeAnnotationsReadOnly() {
        val pageTree = swingController.document.pageTree
        val n = pageTree.numberOfPages
        for (i in 0..n) {
            val page = pageTree.getPage(i)
            page?.annotations?.forEach {
                it?.setFlag(Annotation.FLAG_READ_ONLY, true)
                it?.setFlag(Annotation.FLAG_LOCKED_CONTENTS, true)
                it?.setFlag(Annotation.FLAG_LOCKED, true)
            }
        }
    }
}
