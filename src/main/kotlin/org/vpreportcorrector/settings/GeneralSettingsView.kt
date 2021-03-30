package org.vpreportcorrector.settings

import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.components.form.help
import org.vpreportcorrector.utils.getUserHomeDirectory
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.io.File
import java.nio.file.Paths

class GeneralSettingsView : View() {
    private val vm: GeneralSettingsViewModel by inject()

    override val root = fieldset(t("general")) {
        field(t("workingDirectory")) {
            hbox {
                hgrow = Priority.ALWAYS
                textfield(vm.workingDirectory) {
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
                            vm.workingDirectory.value = selectedDir.absolutePath
                        }
                    }
                }
                help(t("workingDirTooltip"))
            }
        }
        vm.validate(decorateErrors = false)
    }

    private fun getInitialDirectory(): File? {
        return if (vm.workingDirectory.value.isNullOrBlank()) {
            getUserHomeDirectory()
        } else {
            try {
                Paths.get(vm.workingDirectory.value).toFile()
            } catch (e: Exception) {
                null
            }
        }
    }
}
