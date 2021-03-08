package org.vpreportcorrector.diagram

import javafx.beans.property.SimpleBooleanProperty
import org.icepdf.core.pobjects.annotations.Annotation
import org.icepdf.ri.common.SwingController
import org.icepdf.ri.common.ViewModel
import tornadofx.*
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.lang.Exception
import javax.swing.JComponent
import javax.swing.SwingUtilities

class DiagramController: Controller() {
    val model: DiagramModel by inject()
    val swingController = SwingController()

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

    /**
     * Handles diagram tab closing. Checks if changes need to be saved.
     * @return true if the tab can be closed, false otherwise
     */
    fun onClose(): Boolean {
//        if (model.hasUnsavedChangesProperty.value) {
            // TODO: 14.02.21 edit view closing logic, check saved, save diagram errors
//        var doClose = false
//        confirm("Close view?", "text: '${textProperty.value}'", actionFn = { doClose = true })
//        return doClose
        SwingUtilities.invokeAndWait {
            swingController.dispose()
        }
        return true
    }

    fun save() {
        runAsync {
            model.loadingLatch.startLoading()
            savePdfDocument()
            saveDiagramErrors()
        } finally {
            model.loadingLatch.endLoading()
            println("in finally block")
        }
        // TODO: 09.03.21 error handling
    }

    private fun saveDiagramErrors() {
        // TODO: saveDiagramErrors - implemented when sync to disk and persistence method implemented
    }

    private fun savePdfDocument() {
        SwingUtilities.invokeAndWait {
            simplePdfSave()
        }
    }

    private fun simplePdfSave() {
        val file = model.path.toFile()
        try {
            println("saving start")
            val document = swingController.document
            val fileOutputStream = FileOutputStream(file)
            val buf = BufferedOutputStream(fileOutputStream, 8192)
            if (!document.stateManager.isChanged) {
                document.writeToOutputStream(buf)
            } else {
                document.saveToOutputStream(buf)
            }
            buf.flush()
            fileOutputStream.flush()
            buf.close()
            fileOutputStream.close()
            println("saving end")
        } catch (e: Exception) {
            log.severe(e.stackTraceToString())
            // TODO: 09.03.21 error handling
        }
        ViewModel.setDefaultFile(file)
    }

    fun openDocumentForView(viewerPanel: JComponent) {
        SwingUtilities.invokeLater {
            swingController.openDocument(model.path.toAbsolutePath().toString())
            makeAnnotationsReadOnly()
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
