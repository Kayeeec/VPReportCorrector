package org.umlreviewer.sync.googledisk.settings

import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.utils.t
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
        field("Remote folder ID:") {
            hbox {
                hgrow = Priority.ALWAYS
                textfield(vm.remoteFolderId) {
                    hgrow = Priority.ALWAYS
                }
                button("", FontIcon(FontAwesomeBrands.GOOGLE_DRIVE)) {
                    tooltip = tooltip("Browse Drive folders")
                    enableWhen { vm.credential.isNotNull.and(vm.driveService.isNotNull) }
                    action {
                        vm.selectRemoteFolder()
                    }
                }
            }
        }
        vm.validate(decorateErrors = false)
    }
}

