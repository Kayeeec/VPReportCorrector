package org.vpreportcorrector.filesexplorer

import javafx.beans.property.SimpleBooleanProperty
import tornadofx.ViewModel

class FilesExplorerModel : ViewModel() {
    val isEditVisible = SimpleBooleanProperty(false)
    val isViewVisible = SimpleBooleanProperty(false)
    val isImportVisible = SimpleBooleanProperty(false)
    val isRenameVisible = SimpleBooleanProperty(false)
    val isNewFolderVisible = SimpleBooleanProperty(false)
    val isFileTreeFocused = SimpleBooleanProperty(false)
    val isMergePdfsVisible = SimpleBooleanProperty(false)
}





