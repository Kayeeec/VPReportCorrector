package org.vpreportcorrector.settings.modal

import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.settings.general.GeneralSettingsView
import org.vpreportcorrector.sync.RemoteRepo
import org.vpreportcorrector.sync.git.settings.GitSettingsView
import org.vpreportcorrector.utils.t
import tornadofx.*

class SettingsModalView : View() {
    private val vm: SettingsModalViewModel by inject()

    private val gitSettings = vbox {
        add(find<GitSettingsView>())
        enableWhen { vm.repositoryType.isEqualTo(RemoteRepo.GIT) }
    }

    init {
        title = this.t("title")
    }

    override fun onBeforeShow() {
        vm.initialize()
        modalStage?.let {
            it.minWidth = 600.0
            it.minHeight = 400.0
        }
        modalStage?.setOnCloseRequest {
            vm.reset()
        }
    }

    override val root = stackpane {
        prefWidth = 750.0
        borderpane {
            addClass(Styles.paddedContainer)
            center = vbox {
                add(find<GeneralSettingsView>())
                add(gitSettings)
            }
            bottom = buttonbar {
                button(t("cancel")) {
                    isCancelButton = true
                    action {
                        vm.reset()
                        close()
                    }
                }
                button(t("reset")) {
                    enableWhen { vm.isAnyDirty }
                    action {
                        vm.reset()
                    }
                }
                button(t("save")) {
                    enableWhen { vm.isSaveEnabled }
                    isDefaultButton = true
                    action {
                        vm.save {
                            close()
                        }
                    }
                }
            }
            vm.validate(decorateErrors = false)
        }

        loadingOverlay {
            visibleWhen { vm.isAnyLoading }
        }
    }
}


