package org.vpreportcorrector.filesexplorer.dialogs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ToggleGroup
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.Styles.Companion.container
import org.vpreportcorrector.app.Styles.Companion.icon48
import org.vpreportcorrector.app.Styles.Companion.iconBlue
import org.vpreportcorrector.app.Styles.Companion.p
import org.vpreportcorrector.utils.suggestName
import tornadofx.*

enum class FileConflictChoice {
    RENAME, REPLACE_OR_MERGE, SKIP
}

class FileExistsDialogView : View() {
    private val model: FileExistsDialogModel by inject()

    private var isDirectory: Boolean by singleAssign()
    private var descriptionMsg: String by singleAssign()
    private var overwriteBtnText: String by singleAssign()
    private var copiedName: String by singleAssign()
    private var skipBtn: Button by singleAssign()

    private val toggleGroup = ToggleGroup()

    private val isRenameEnabled = SimpleBooleanProperty(false)
    private val isCloseEnabled = SimpleBooleanProperty(false)
    private val isOkEnabled = SimpleBooleanProperty(true)
    private val rememberChkbxLabel = SimpleStringProperty("Remember for all")

    init {
        isDirectory = model.copied.value.toFile().isDirectory
        copiedName = model.copied.value.toFile().name
        if (isDirectory) {
            title = "Directory exists: merge or rename"
            descriptionMsg = "Directory '$copiedName' already exists.\nDo you want to merge the directories or rename the target?"
            overwriteBtnText = "Merge directories"
        } else {
            title = "File exists: replace or rename"
            descriptionMsg = "File '$copiedName' already exists.\nDo you want to replace the existing file or rename the target?"
            overwriteBtnText = "Replace file"
        }
    }

    override fun onBeforeShow() {
        modalStage?.setOnCloseRequest {
            if (!isCloseEnabled.value) it.consume()
        }
        model.newName.onChange { newValue ->
            isRenameEnabled.value = newValue != null && newValue.trim() != copiedName && newValue.trim().isNotBlank()
        }
        model.newName.value = suggestName(model.existing.value.parent, model.copied.value)
        toggleGroup.selectedValueProperty<FileConflictChoice>().value = FileConflictChoice.REPLACE_OR_MERGE
        model.choice.value = FileConflictChoice.REPLACE_OR_MERGE
        toggleGroup.selectedValueProperty<FileConflictChoice>().onChange {
            if (it == FileConflictChoice.RENAME) {
                isOkEnabled.bind(isRenameEnabled)
                rememberChkbxLabel.value = "Remember for all (new name selected automatically)"
            } else {
                isOkEnabled.unbind()
                isOkEnabled.value = true
                rememberChkbxLabel.value = "Remember for all ${if (isDirectory) "directories" else "files"}"
            }
        }
        model.remember.onChange { remember ->
            if (remember) {
                skipBtn.text = "Skip for all"
            } else {
                skipBtn.text = "Skip"
            }
        }
    }

    override val root = borderpane {
        addClass(container)

        center = form {
            fieldset() {
                hbox(20) {
                    field {
                        text(descriptionMsg) {
                            addClass(p)
                        }
                    }
                    field {
                        add(FontIcon(FontAwesomeSolid.QUESTION_CIRCLE).apply {
                            addClass(icon48)
                            addClass(iconBlue)
                        })
                    }
                }
            }

            fieldset {
                field {
                    radiobutton(overwriteBtnText, toggleGroup, value = FileConflictChoice.REPLACE_OR_MERGE)
                }
            }

            fieldset(labelPosition = Orientation.VERTICAL){
                field {
                    radiobutton("Rename", toggleGroup, value = FileConflictChoice.RENAME)
                }
                field("New name:") {
                    style {
                        padding = box(10.px, 0.px, 0.px, 25.px)
                    }
                    textfield(model.newName) {
                        focusedProperty().onChange { focused ->
                            if (!focused) {
                                model.newName.value = model.newName.value?.trim() ?: ""
                            }
                        }
                        enableWhen { toggleGroup.selectedValueProperty<FileConflictChoice>().isEqualTo(FileConflictChoice.RENAME) }
                    }
                }
            }

            fieldset {
                field {
                style {
                    padding = box(10.px, 0.px, 0.px, 0.px)
                }
                    checkbox(property = model.remember) {
                        textProperty().bind(rememberChkbxLabel)
                    }
                }
            }
        }

        bottom = buttonbar {
            skipBtn = button("Skip for all") {
                action {
                    model.commit()
                    model.choice.value = FileConflictChoice.SKIP
                    close()
                }
            }
            button("Ok") {
                isDefaultButton = true
                enableWhen { isOkEnabled }
                action {
                    model.commit()
                    isCloseEnabled.value = true
                    if (toggleGroup.selectedValueProperty<FileConflictChoice>().value == FileConflictChoice.RENAME) {
                        model.choice.value = FileConflictChoice.RENAME
                    } else {
                        model.choice.value = FileConflictChoice.REPLACE_OR_MERGE
                    }
                    close()
                }
            }
        }

        ButtonBar.setButtonUniformSize(skipBtn, false)
        skipBtn.text = "Skip" // for proper btn size
    }
}
