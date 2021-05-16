package org.umlreviewer.filesexplorer.dialogs

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import org.apache.commons.io.FilenameUtils
import org.umlreviewer.styles.Styles
import org.umlreviewer.utils.file.isValidFileName
import tornadofx.*
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

class RenameDialogView : View("Rename") {
    private val model: RenameDialogViewModel by inject()

    override fun onBeforeShow() {
        modalStage?.let {
            it.minWidth = 350.0
        }
    }

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
                            else if (!isValidFileName(it))
                                error("The name contains invalid characters.")
                            else null
                        }
                    }
                }
            }
        }

        bottom = buttonbar {
            addClass(Styles.paddedContainer)
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
                        val newName = resolveName(data)
                        Files.move(data.toRename, data.toRename.resolveSibling(newName))
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

    private fun resolveName(data: RenameDialogModel): String {
        val trimmed = data.newName.trim()
        val file = data.toRename.toFile()
        if (file.isDirectory) return trimmed
        var extension = FilenameUtils.getExtension(trimmed)
        if (extension.isNullOrBlank()) {
            extension = FilenameUtils.getExtension(file.name)
            return "${trimmed}.${extension}"
        }
        return trimmed
    }
}

class RenameDialogModel(renamed: Path) {
    val conflictingFileNameProperty = SimpleStringProperty(null)
    var conflictingFileName: String? by conflictingFileNameProperty

    val oldNameProperty = SimpleStringProperty(renamed.toFile().name)
    var oldName: String by oldNameProperty

    val newNameProperty = SimpleStringProperty(renamed.toFile().name)
    var newName: String by newNameProperty

    val toRenameProperty = SimpleObjectProperty(renamed)
    var toRename: Path by toRenameProperty
}

class RenameDialogViewModel(renameDialog: RenameDialogModel) : ItemViewModel<RenameDialogModel>(renameDialog) {
    val newName = bind(RenameDialogModel::newNameProperty)
    val oldName = bind(RenameDialogModel::oldNameProperty)
    val toRename = bind(RenameDialogModel::toRenameProperty)
    val conflictingFileName = bind(RenameDialogModel::conflictingFileNameProperty)
}
