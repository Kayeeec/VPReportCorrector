package org.vpreportcorrector.sync

import javafx.scene.layout.Priority
import org.vpreportcorrector.utils.t
import tornadofx.*


class GoogleDriveSettingsView : View("Google Drive Settings") {
    private val vm: GoogleDriveSettingsViewModel by inject()

    init {
        vm.initializeData()
    }

    override val root = fieldset(t("remoteRepository")) {
        field {
            textProperty.bind(vm.loginFieldLabelProperty)
            hbox { hgrow = Priority.ALWAYS }
            hbox {
                dynamicContent(vm.credential) {
                    if (vm.credential.value == null) {
                        button("Log in with Google") {
                            enableWhen { vm.credential.isNull }
                            action {
                                vm.logIn()
                            }
                        }
                    } else {
                        button("Log out") {
                            enableWhen { vm.credential.isNotNull }
                            action {
                                vm.logOut()
                            }
                        }
                    }
                }
            }
        }
        field {
            button("Log files") {
                enableWhen { vm.credential.isNotNull }
                action {
                    vm.logFiles()
                }
            }
        }
    }
}

