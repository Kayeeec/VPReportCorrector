package org.vpreportcorrector.filesexplorer.dialogs

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.FileAlreadyExistsException

class NewFolderDialogView : View("New folder") {
    private val model: NewFolderModel by inject()

    init {
        title = "New folder (${model.location.value.absolutePath})"
    }

    override fun onBeforeShow() {
        modalStage?.minWidth = 300.0
        modalStage?.minHeight = 50.0
    }

    override val root = borderpane {
        center = form {
            vgrow = Priority.ALWAYS
            fieldset(
                labelPosition = Orientation.VERTICAL
            ) {
                field("New folder name:") {
                    textfield(model.name) {
                        required()
                        whenDocked { requestFocus() }
                    }
                }
            }
        }

        bottom = buttonbar {
            button("Cancel"){
                isCancelButton = true
                action { close() }
            }
            button("Save") {
                isDefaultButton = true
                enableWhen(model.valid)
                action {
                    model.commit()
                    var newFolderFile: File? = null
                    try {
                        val newFolderModel = model.item
                        newFolderFile = File(newFolderModel.location, newFolderModel.name)
                        Files.createDirectories(newFolderFile.toPath())
                    } catch (e: FileAlreadyExistsException) {
                        log.severe(e.stackTraceToString())
                        error(
                            title = "Error creating directory",
                            header = "Directory '${newFolderFile?.absolutePath}' already exists.",
                            content = "Error message:\n${e.message}",
                        )

                    } catch (e: SecurityException) {
                        log.severe(e.stackTraceToString())
                        error(
                            title = "Error creating directory",
                            header = "Insufficient permissions to create directory: '${newFolderFile?.absolutePath}'",
                            content = "Error message:\n${e.message}",
                        )
                    } catch (e: Exception) {
                        log.severe(e.stackTraceToString())
                        error(
                            title = "Error creating directory",
                            header = "Failed to create directory: '${newFolderFile?.absolutePath}'",
                            content = "Error message:\n${e.message}",
                        )
                    } finally {
                        close()
                    }
                }
            }
        }

        model.validate(decorateErrors = false)
    }
}

class NewFolder(location: File, name: String = "") {
    val locationProperty = SimpleObjectProperty(location)
    var location: File by locationProperty

    val nameProperty = SimpleStringProperty(name)
    var name: String by nameProperty
}

class NewFolderModel(newFolder: NewFolder) : ItemViewModel<NewFolder>(newFolder) {
    val location = bind(NewFolder::locationProperty)
    val name = bind(NewFolder::nameProperty)
}

