package org.umlreviewer.settings.general

import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.components.form.help
import org.umlreviewer.sync.RemoteRepo
import org.umlreviewer.utils.file.getUserHomeDirectory
import org.umlreviewer.utils.t
import tornadofx.*
import java.io.File
import java.nio.file.Paths

class GeneralSettingsView : View() {
    private val vm: GeneralSettingsViewModel by inject()

    override val root = form {
        fieldset(t("general")) {
            field(t("workingDirectory")) {
                hbox {
                    hgrow = Priority.ALWAYS
                    textfield(vm.workingDirectory) {
                        isEditable = false
                        hgrow = Priority.ALWAYS
                    }
                    button("", FontIcon(FontAwesomeSolid.TIMES)) {
                        tooltip = tooltip("Clear")
                        action { vm.workingDirectory.value = "" }
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
            field(t("remoteRepository")) {
                combobox(vm.remoteRepository, listOf(*RemoteRepo.values()).asObservable()) {
                    cellFormat {
                        text = t(it.name)
                    }
                }
            }
            vm.validate(decorateErrors = false)
        }
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
