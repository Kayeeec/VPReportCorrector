package org.vpreportcorrector.filesexplorer.dialogs

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import tornadofx.*
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

class RenameDialogView : View("Rename") {
    val model: RenameDialogModel by inject()

    override val root = borderpane {
        center = form {
            fieldset(labelPosition = Orientation.VERTICAL) {
                field("New name:") {
                    textfield(model.newName) {
                        validator {
                            if (it.isNullOrBlank())
                                error("The 'New name' field is required.")
                            else if (model.conflictingFileName.value == model.newName.value)
                                error("File with this name already exists. Choose a different name.")
                            else if (it.trim() == model.oldName.value.trim()
                                || it.trim() == model.toRename.value.toFile().name.trim())
                                error("The new name must be different from the current name.")
                            else null
                        }
                    }
                }
            }
        }

        bottom = buttonbar {
            button("Cancel") {
                isCancelButton = true
                action {
                    close()
                }
            }
            button("Rename") {
                isDefaultButton = true
                enableWhen { model.valid }
                action {
                    try {
                        model.commit()
                        Files.move(model.toRename.value, model.toRename.value.resolveSibling(model.newName.value.trim()))
                        close()
                    } catch (e: FileAlreadyExistsException) {
                        model.conflictingFileName.value = model.newName.value
                        model.validate()
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        error(
                            title = "Error renaming file",
                            header = "Insufficient permissions to rename file.",
                            content = "File:\n${model.toRename.value}\n\nError message:\n${e.message}",
                        )
                        close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        error(
                            title = "Error renaming file",
                            header = "Failed to rename file",
                            content = "File:\n${model.toRename.value}\n\nError message:\n${e.message}",
                        )
                        close()
                    }
                }
            }
        }

        model.validate(decorateErrors = false)
        model.newName.onChange {
            if (!model.conflictingFileName.value.isNullOrBlank()) model.conflictingFileName.value = null
        }
    }
}

class RenameDialog(renamed: Path) {
    val conflictingFileNameProperty = SimpleStringProperty(null)
    var conflictingFileName by conflictingFileNameProperty

    val oldNameProperty = SimpleStringProperty(renamed.toFile().name)
    var oldName by oldNameProperty

    val newNameProperty = SimpleStringProperty(renamed.toFile().name)
    var newName by newNameProperty

    val toRenameProperty = SimpleObjectProperty<Path>(renamed)
    var toRename by toRenameProperty
}

class RenameDialogModel(renameDialog: RenameDialog) : ItemViewModel<RenameDialog>(renameDialog) {
    val newName = bind(RenameDialog::newNameProperty)
    val oldName = bind(RenameDialog::oldNameProperty)
    val toRename = bind(RenameDialog::toRenameProperty)
    val conflictingFileName = bind(RenameDialog::conflictingFileNameProperty)
}
