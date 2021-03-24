package org.vpreportcorrector.settings

import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.components.form.help
import org.vpreportcorrector.utils.getUserHomeDirectory
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.io.File
import java.nio.file.Paths

class SettingsModalView : View() {
    private val model: SettingsModel by inject()

    init {
        title = this.t("title")
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.minWidth = 600.0
            it.minHeight = 400.0
        }
    }
    override val root = borderpane {
        addClass(Styles.paddedContainer)
        center = form {
            fieldset(t("general")) {
                field(t("workingDirectory")) {
                    hbox {
                        hgrow = Priority.ALWAYS
                        textfield(model.workingDirectory) {
                            isEditable = false
                            hgrow = Priority.ALWAYS
                        }
                        button("", FontIcon(FontAwesomeSolid.FOLDER)) {
                            tooltip = Tooltip(t("selectWorkDir"))
                            action {
                                val selectedDir = chooseDirectory(
                                    title = t("chooseDirectory"),
                                    initialDirectory = getInitialDirectory()
                                )
                                if (selectedDir != null) {
                                    model.workingDirectory.value = selectedDir.absolutePath
                                }
                            }
                        }
                        help(t("workingDirTooltip"))
                    }
                }
            }
            fieldset(t("remoteRepository")) {
                text("TODO")
            }
        }
        bottom = buttonbar {
            button(t("cancel")) {
                isCancelButton = true
                action {
                    close()
                }
            }
            button(t("reset")) {
                enableWhen { model.dirty }
                action {
                    model.rollback()
                }
            }
            button(t("save")) {
                enableWhen { model.valid.and(model.dirty) }
                isDefaultButton = true
                action {
                    model.commit()
                    close()
                }
            }
        }
        model.validate(decorateErrors = false)
    }

    private fun getInitialDirectory(): File? {
        return if (model.workingDirectory.value.isNullOrBlank()) {
            getUserHomeDirectory()
        } else {
            try {
                Paths.get(model.workingDirectory.value).toFile()
            } catch (e: Exception) {
                null
            }
        }
    }
}


