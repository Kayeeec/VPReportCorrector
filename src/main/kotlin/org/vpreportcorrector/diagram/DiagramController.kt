package org.vpreportcorrector.diagram

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.*

class DiagramController: Controller() {
    val model: DiagramModel by inject()

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
    open fun onClose(): Boolean {
//        if (model.hasUnsavedChangesProperty.value) {
            // TODO: 14.02.21 edit view closing logic
//        var doClose = false
//        confirm("Close view?", "text: '${textProperty.value}'", actionFn = { doClose = true })
//        return doClose
        return true
    }

    fun save() {
        // TODO: 28.02.21
    }
}
