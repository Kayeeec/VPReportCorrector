package org.vpreportcorrector.filesexplorer

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue
import java.nio.file.Path

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

    val isFileTreeFocusedProperty = SimpleBooleanProperty(false)
    var isFileTreeFocused by isFileTreeFocusedProperty

    val copiedPathsProperty = SimpleListProperty<Path>(FXCollections.emptyObservableList())
    var copiedPaths by copiedPathsProperty

    val isPasteEnabledProperty = SimpleBooleanProperty(false)
    var isPasteEnabled by isPasteEnabledProperty
}

class FilesExplorerModel : ItemViewModel<FilesExplorer>() {
    val isEditVisible = bind(FilesExplorer::isEditVisibleProperty)
    val isViewVisible = bind(FilesExplorer::isViewVisibleProperty)
    val isImportVisible = bind(FilesExplorer::isImportVisibleProperty)
    val isRenameVisible = bind(FilesExplorer::isRenameVisibleProperty)
    val isNewFolderVisible = bind(FilesExplorer::isNewFolderVisibleProperty)
    val isFileTreeFocused = bind(FilesExplorer::isFileTreeFocusedProperty)
    val copiedPaths = bind(FilesExplorer::copiedPathsProperty)
    val isPasteEnabled = bind(FilesExplorer::isPasteEnabledProperty)
}





