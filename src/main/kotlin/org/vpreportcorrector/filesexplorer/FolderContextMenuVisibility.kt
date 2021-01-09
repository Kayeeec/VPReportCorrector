package org.vpreportcorrector.filesexplorer

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class FolderContextMenuVisibility() {
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

class FileContextMenuVisibilityModel : ItemViewModel<FolderContextMenuVisibility>() {
    val isEditVisible = bind(FolderContextMenuVisibility::isEditVisibleProperty)
    val isViewVisible = bind(FolderContextMenuVisibility::isViewVisibleProperty)
    val isImportVisible = bind(FolderContextMenuVisibility::isImportVisibleProperty)
    val isRenameVisible = bind(FolderContextMenuVisibility::isRenameVisibleProperty)
    val isNewFolderVisible = bind(FolderContextMenuVisibility::isNewFolderVisibleProperty)
}




