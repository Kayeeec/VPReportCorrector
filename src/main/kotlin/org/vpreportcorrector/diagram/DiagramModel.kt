package org.vpreportcorrector.diagram

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import org.vpreportcorrector.components.LoadingLatch
import tornadofx.ViewModel
import tornadofx.asObservable
import java.nio.file.Path

class DiagramModel(val path: Path, val isEditing: Boolean): ViewModel() {
    val hasUnsavedChangesProperty = SimpleBooleanProperty(false)
    var loadingLatch = LoadingLatch()
    val pathUriString: String
        get() = path.toUri().toString()

    /**
     * Maps error label translation key to a boolean which signifies whether the error is selected or not.
     */
    val diagramErrorsProperty = SimpleListProperty<Pair<String, SimpleBooleanProperty>>(
        mutableListOf<Pair<String, SimpleBooleanProperty>>().asObservable()
    )
}
