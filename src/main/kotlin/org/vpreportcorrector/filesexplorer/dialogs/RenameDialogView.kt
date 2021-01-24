package org.vpreportcorrector.filesexplorer.dialogs

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import tornadofx.*
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

class RenameDialogView : View("Rename") {
    private val model: RenameDialogModel by inject()

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
                    model.commit()
                    val data = model.item
                    try {
                        Files.move(data.toRename, data.toRename.resolveSibling(data.newName.trim()))
                        close()
                    } catch (e: FileAlreadyExistsException) {
                        model.conflictingFileName.value = data.newName
                        model.validate()
                    } catch (e: SecurityException) {
                        log.severe(e.stackTraceToString())
                        error(
                            title = "Error renaming file",
                            header = "Insufficient permissions to rename file.",
                            content = "File:\n${data.toRename}\n\nError message:\n${e.message}",
                        )
                        close()
                    } catch (e: Exception) {
                        log.severe(e.stackTraceToString())
                        error(
                            title = "Error renaming file",
                            header = "Failed to rename file",
                            content = "File:\n${data.toRename}\n\nError message:\n${e.message}",
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
    var conflictingFileName: String? by conflictingFileNameProperty

    val oldNameProperty = SimpleStringProperty(renamed.toFile().name)
    var oldName: String by oldNameProperty

    val newNameProperty = SimpleStringProperty(renamed.toFile().name)
    var newName: String by newNameProperty

    val toRenameProperty = SimpleObjectProperty(renamed)
    var toRename: Path by toRenameProperty
}

class RenameDialogModel(renameDialog: RenameDialog) : ItemViewModel<RenameDialog>(renameDialog) {
    val newName = bind(RenameDialog::newNameProperty)
    val oldName = bind(RenameDialog::oldNameProperty)
    val toRename = bind(RenameDialog::toRenameProperty)
    val conflictingFileName = bind(RenameDialog::conflictingFileNameProperty)
}
