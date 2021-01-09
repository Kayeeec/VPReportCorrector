package org.vpreportcorrector.filesexplorer

import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class NewFolder(location: String? = null, name: String? = null ) {
    val locationProperty = SimpleStringProperty(location)
    var location by locationProperty

    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

}

class NewFolderModel(newFolder: NewFolder) : ItemViewModel<NewFolder>(newFolder) {
    val location = bind(NewFolder::locationProperty)
    val name = bind(NewFolder::nameProperty)
}


