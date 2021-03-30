package org.vpreportcorrector.sync.googledisk.settings

import com.google.api.services.drive.model.File
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.Styles.Companion.paddedContainer
import org.vpreportcorrector.utils.t
import tornadofx.*
import tornadofx.getValue
import tornadofx.setValue

class SelectRemoteFolderModal : View("Select remote folder") {
    private val vm: SelectRemoteFolderViewModel by inject()

    override val root = borderpane {
        addClass(paddedContainer)
        prefWidth = 400.0
        top = form {
            fieldset {
                field("Search by name:"){
                    textfield(vm.searchTextProperty)
                }
                field("Selected folder:") {
                    textfield(vm.selectedFolderString) {
                        isEditable = false
                    }
                }
            }
        }
        center = vbox {
            style { padding = box(0.px, 0.px, 10.px, 0.px) }
            listview(vm.filteredFolders) {
                multiSelect(false)
                vm.selectedFolder.value?.let {
                    selectionModel.select(it)
                }
                onUserSelect(1) { file: File ->
                    vm.selectedFolder.value = file
                }
                cellFormat {
                    graphic = FontIcon(FontAwesomeSolid.FOLDER) // TODO KB: icon
                    text = it.name
                }
            }
        }
        bottom = buttonbar {
            button(t("cancel")) {
                isCancelButton = true
                action {
                    vm.selectedFolder.value = null
                    vm.commit()
                    close()
                }
            }
            button(t("ok")) {
                isDefaultButton = true
                enableWhen { vm.selectedFolderId.isNotEmpty }
                action {
                    vm.commit()
                    close()
                }
            }
        }
    }
}

class SelectRemoteFolderModel(files: List<File>, selectedFolderId: String?) {
    val selectedFolderIdProperty = SimpleStringProperty(selectedFolderId)
    var selectedFolderId: String? by selectedFolderIdProperty

    val foldersProperty = SimpleListProperty(files.asObservable())
}


class SelectRemoteFolderViewModel(model: SelectRemoteFolderModel): ItemViewModel<SelectRemoteFolderModel>(model) {
    private val folders = bind(SelectRemoteFolderModel::foldersProperty)
    val searchTextProperty = SimpleStringProperty()
    val filteredFolders = SortedFilteredList<File>(this.folders).apply {
        filterWhen(searchTextProperty) {s: String?, file: File ->
            if (s.isNullOrBlank()) true
            else file.name.contains(s, true)
        }
    }

    val selectedFolder = SimpleObjectProperty<File>()
    val selectedFolderId = bind(SelectRemoteFolderModel::selectedFolderIdProperty)
    val selectedFolderString = SimpleStringProperty()

    init {
        selectedFolder.value = folders.value.find { it.id == selectedFolderId.value }
        selectedFolderString.value = selectedFolder.value?.name

        selectedFolder.onChange {
            selectedFolderId.value = it?.id
            selectedFolderString.value = it?.name
        }
    }
}
