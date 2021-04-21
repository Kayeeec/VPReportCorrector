package org.vpreportcorrector.filesexplorer.dialogs

import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.utils.FileTreeHelpers.getFolderNameInfoOrWarning
import org.vpreportcorrector.utils.FolderMessageType
import org.vpreportcorrector.utils.FolderNameMessage
import org.vpreportcorrector.utils.isValidFileName
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.io.File
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files

class NewFolderDialogView : View() {
    private val vm: NewFolderViewModel by inject()

    init {
        title = t("title", vm.location.value.absolutePath)
    }

    override fun onBeforeShow() {
        modalStage?.minWidth = 300.0
        modalStage?.minHeight = 200.0
        modalStage?.sizeToScene()
        modalStage?.centerOnScreen()
    }

    override val root = borderpane {
        prefHeight = 200.0
        prefWidth = 460.0

        center = form {
            vgrow = Priority.NEVER
            fieldset(
                labelPosition = Orientation.VERTICAL
            ) {
                field(t("nameLabel")) {
                    textfield(vm.name) {
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
                field {
                    hbox {
                        visibleWhen { vm.warningMessage.isNotNull }
                        hgrow = Priority.ALWAYS
                        label {
                            text = ""
                            graphicProperty().bind(vm.warningIcon)
                            style {
                                padding = box(0.px, 5.px, 0.px, 2.px)
                            }
                        }
                        label {
                            textProperty().bind(vm.warningMessage)
                            isWrapText = true
                            prefWidth = 300.0
                            prefWidthProperty().bind(this@field.widthProperty().minus(40.0))
                        }
                    }
                }
            }
        }

        bottom = buttonbar {
            addClass(Styles.paddedContainer)
            button(t("cancel")){
                isCancelButton = true
                action { close() }
            }
            button(t("create")) {
                isDefaultButton = true
                enableWhen(vm.valid)
                action {
                    vm.commit()
                    var newFolderFile: File? = null
                    try {
                        val newFolderModel = vm.item
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

        vm.validate(decorateErrors = false)
    }
}

class NewFolderModel(location: File, name: String = "") {
    val locationProperty = SimpleObjectProperty(location)
    var location: File by locationProperty

    val nameProperty = SimpleStringProperty(name)
    var name: String by nameProperty
}

class NewFolderViewModel(newFolderModel: NewFolderModel) : ItemViewModel<NewFolderModel>(newFolderModel) {
    val location = bind(NewFolderModel::locationProperty)
    val name = bind(NewFolderModel::nameProperty)

    private val nameMessage: ObjectBinding<FolderNameMessage?> = objectBinding(this, name) {
        val result = getFolderNameInfoOrWarning(location.value, name.value)
        result
    }
    val warningIcon = objectBinding(this, nameMessage) {
        var result: FontIcon? = null
        if (nameMessage.value != null) {
            result = if (nameMessage.value?.type === FolderMessageType.SUCCESS) {
                FontIcon(FontAwesomeSolid.CHECK_CIRCLE)
            } else {
                FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE)
            }
        }
        result
    }
    val warningMessage = stringBinding(this, nameMessage) {
        var result: String? = null
        if (nameMessage.value != null) result = nameMessage.value?.message
        result
    }
}

