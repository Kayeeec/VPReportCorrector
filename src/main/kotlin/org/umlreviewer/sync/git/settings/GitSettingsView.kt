package org.umlreviewer.sync.git.settings

import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.utils.file.getUserHomeDirectory
import tornadofx.*

class GitSettingsView : View() {
    private val vm: GitSettingsViewModel by inject()

    private val initialDir = getUserHomeDirectory()

    override val root = form {
        fieldset("Git repository settings") {
            field("Repository URL:") {
                hbox {
                    hgrow = Priority.ALWAYS
                    textfield(vm.repoUrl) {
                        hgrow = Priority.ALWAYS
                    }
                    hbox {
                        togglegroup {
                            togglebutton(GitProtocol.HTTPS.name, value = GitProtocol.HTTPS) {
                                whenSelected { vm.method.isEqualTo(GitProtocol.HTTPS) }
                                action {
                                    vm.method.value = GitProtocol.HTTPS
                                    isSelected = true
                                    vm.validate(focusFirstError = false, decorateErrors = true)
                                }
                            }
                            togglebutton(GitProtocol.SSH.name, value = GitProtocol.SSH) {
                                whenSelected { vm.method.isEqualTo(GitProtocol.SSH) }
                                action {
                                    vm.method.value = GitProtocol.SSH
                                    isSelected = true
                                    vm.validate(focusFirstError = false, decorateErrors = true)
                                }
                            }
                        }
                    }
                }
            }
        }
        fieldset {
            dynamicContent(vm.method) {
                if (vm.method.value == GitProtocol.HTTPS) {
                    field("Username:") {
                        textfield(vm.username) {
                            requiredWhen(vm.method.isEqualTo(GitProtocol.HTTPS).and(vm.repoUrl.isNotBlank()))
                        }
                    }

                    field("Password:") {
                        hbox {
                            dynamicContent(vm.showPassword) {
                                if (vm.showPassword.value == true) {
                                    textfield(vm.password) {
                                        hgrow = Priority.ALWAYS
                                        requiredWhen(vm.method.isEqualTo(GitProtocol.HTTPS).and(vm.repoUrl.isNotBlank()))
                                    }
                                } else {
                                    passwordfield(vm.password) {
                                        hgrow = Priority.ALWAYS
                                        requiredWhen(vm.method.isEqualTo(GitProtocol.HTTPS).and(vm.repoUrl.isNotBlank()))
                                    }
                                }
                                button("") {
                                    tooltip = tooltip("Show/hide")
                                    graphicProperty().bind(vm.showPasswordGraphic)
                                    action {
                                        vm.showPassword.value = !vm.showPassword.value
                                    }
                                }
                            }
                        }
                    }
                } else {
                    field("Private key:") {
                        hbox {
                            textfield(vm.privateKeyPath) {
                                hgrow = Priority.ALWAYS
                                requiredWhen(vm.method.isEqualTo(GitProtocol.SSH).and(vm.repoUrl.isNotBlank()))
                                isEditable = false
                            }
                            button("", FontIcon(FontAwesomeSolid.TIMES)) {
                                tooltip = tooltip("Clear")
                                action { vm.privateKeyPath.value = "" }
                            }
                            button("", FontIcon(FontAwesomeSolid.FOLDER)) {
                                tooltip = tooltip("Select path to SSH private key")
                                action {
                                    val files = chooseFile(
                                        title = "Select SSH private key",
                                        filters = arrayOf(),
                                        initialDirectory = initialDir,
                                        mode = FileChooserMode.Single
                                    )
                                    if (files.isNotEmpty()) {
                                        vm.privateKeyPath.value = files.first().absolutePath
                                    }
                                }
                            }
                        }
                    }

                    field("Passphrase:") {
                        hbox {
                            dynamicContent(vm.showPassphrase) {
                                if (vm.showPassphrase.value == true) {
                                    textfield(vm.passphrase) {
                                        hgrow = Priority.ALWAYS
                                    }
                                } else {
                                    passwordfield(vm.passphrase) {
                                        hgrow = Priority.ALWAYS
                                    }
                                }
                                button("") {
                                    tooltip = tooltip("Show/hide")
                                    graphicProperty().bind(vm.showPassphraseGraphic)
                                    action {
                                        vm.showPassphrase.value = !vm.showPassphrase.value
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        fieldset("Git configuration") {
            hbox(20) {
                hgrow = Priority.ALWAYS
                vbox{
                    hgrow = Priority.ALWAYS
                    field("User name:") {
                        textfield(vm.configUserName) {
                            hgrow = Priority.ALWAYS
                            requiredWhen(vm.repoUrl.isNotBlank())
                        }
                    }
                }
                vbox {
                    hgrow = Priority.ALWAYS
                    field("User email:") {
                        textfield(vm.configUserEmail){
                            hgrow = Priority.ALWAYS
                            requiredWhen(vm.repoUrl.isNotBlank())
                        }
                    }
                }
            }
        }
        vm.validate()
    }
}

