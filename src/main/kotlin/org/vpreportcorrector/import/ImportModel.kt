package org.vpreportcorrector.import

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File

class DestinationAndFiles(dest: File? = null, files: MutableList<File> = mutableListOf() ) {
    val destinationProperty = SimpleObjectProperty<File>(dest)
    var destination: File? by destinationProperty

    val filesProperty = SimpleListProperty(files.asObservable())
    var files: ObservableList<File> by filesProperty
}

class SimpleImportModel(dafs: DestinationAndFiles) : ItemViewModel<DestinationAndFiles>(dafs) {
    val destination = bind(DestinationAndFiles::destinationProperty)
    val files = bind(DestinationAndFiles::filesProperty)
    val destinationString = bind { SimpleStringProperty(destination.value?.absolutePath ?: "") }
}

