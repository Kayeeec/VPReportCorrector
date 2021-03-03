package org.vpreportcorrector.filesexplorer.dialogs

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.utils.isValidFileName
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files

class NewFolderDialogView : View() {
    private val model: NewFolderModel by inject()

    init {
        title = t("title", model.location.value.absolutePath)
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
                field(t("nameLabel")) {
                    textfield(model.name) {
                        validator {
                            if (it.isNullOrBlank())
                                error(t("error.required"))
                            else if (!isValidFileName(it))
                                error(t("error.hasInvalidCharacters"))
                            else
                                null
                        }
                        whenDocked { requestFocus() }
                    }
                }
            }
        }

        bottom = buttonbar {
            addClass(Styles.paddedContainer)
            button(t("ok")){
                isCancelButton = true
                action { close() }
            }
            button(t("save")) {
                isDefaultButton = true
                enableWhen(model.valid)
                action {
                    model.commit()
                    var newFolderFile: File? = null
                    try {
                        val newFolderModel = model.item
                        newFolderFile = File(newFolderModel.location, newFolderModel.name.trim())
                        Files.createDirectories(newFolderFile.toPath())
                    } catch (e: FileAlreadyExistsException) {
                        log.severe(e.stackTraceToString())
                        error(
                            title = t("errorTitle"),
                            header = t("directoryExists", newFolderFile?.absolutePath),
                            content = t("errorContent", e.message),
                        )

                    } catch (e: SecurityException) {
                        log.severe(e.stackTraceToString())
                        error(
                            title = t("errorTitle"),
                            header = t("insufficientPermissions", newFolderFile?.absolutePath),
                            content = t("errorContent", e.message),
                        )
                    } catch (e: Exception) {
                        log.severe(e.stackTraceToString())
                        error(
                            title = t("errorTitle"),
                            header = t("creationFailed", newFolderFile?.absolutePath),
                            content = t("errorContent", e.message),
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

