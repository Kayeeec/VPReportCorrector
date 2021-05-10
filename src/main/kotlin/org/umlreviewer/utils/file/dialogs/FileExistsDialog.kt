package org.umlreviewer.utils.file.dialogs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.umlreviewer.utils.file.FileConflictChoice
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue
import java.nio.file.Path

class FileExistsDialog(existing: Path, copied: Path) {
    val existingProperty = SimpleObjectProperty<Path>(existing)
    var existing by existingProperty

    val copiedProperty = SimpleObjectProperty<Path>(copied)
    var copied by copiedProperty

    val newNameProperty = SimpleStringProperty("")
    var newName by newNameProperty

    val choiceProperty = SimpleObjectProperty<FileConflictChoice>(FileConflictChoice.REPLACE_OR_MERGE)
    var choice by choiceProperty

    val rememberProperty = SimpleBooleanProperty(false)
    var remember by rememberProperty
}

class FileExistsDialogModel(fileExistsDialog: FileExistsDialog): ItemViewModel<FileExistsDialog>(fileExistsDialog) {
    val existing = bind(FileExistsDialog::existingProperty)
    val copied = bind(FileExistsDialog::copiedProperty)
    val newName = bind(FileExistsDialog::newNameProperty)
    val choice = bind(FileExistsDialog::choiceProperty)
    val remember = bind(FileExistsDialog::rememberProperty)
}

class FileConflictResult(model: FileExistsDialogModel) {
    var newName: String = model.newName.value
    var choice: FileConflictChoice = model.choice.value
    var remember: Boolean = model.remember.value
}

data class RememberChoice(var directory: FileConflictChoice? = null, var file: FileConflictChoice? = null) {
    fun getRelevantChoice(path: Path): FileConflictChoice? {
        if (path.toFile().isDirectory) {
            return directory
        }
        return file
    }
}
