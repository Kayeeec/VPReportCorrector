package org.vpreportcorrector.filesexplorer

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class FilesExplorer() {
    val isEditVisibleProperty = SimpleBooleanProperty(false)
    var isEditVisible by isEditVisibleProperty

    val isViewVisibleProperty = SimpleBooleanProperty(false)
    var isViewVisible by isViewVisibleProperty

    val isImportVisibleProperty = SimpleBooleanProperty(false)
    var isImportVisible by isImportVisibleProperty

    val isRenameVisibleProperty = SimpleBooleanProperty(false)
    var isRenameVisible by isRenameVisibleProperty

    val isNewFolderVisibleProperty = SimpleBooleanProperty(false)
    var isNewFolderVisible by isNewFolderVisibleProperty
}

class FilesExplorerModel : ItemViewModel<FilesExplorer>() {
    val isEditVisible = bind(FilesExplorer::isEditVisibleProperty)
    val isViewVisible = bind(FilesExplorer::isViewVisibleProperty)
    val isImportVisible = bind(FilesExplorer::isImportVisibleProperty)
    val isRenameVisible = bind(FilesExplorer::isRenameVisibleProperty)
    val isNewFolderVisible = bind(FilesExplorer::isNewFolderVisibleProperty)
}





