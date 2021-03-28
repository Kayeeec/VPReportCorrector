package org.vpreportcorrector.settings

import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.sync.git.settings.GitSettingsView
import org.vpreportcorrector.utils.t
import tornadofx.*

class SettingsModalView : View() {
    private val vm: SettingsViewModel by inject()

    init {
        title = this.t("title")
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.minWidth = 600.0
            it.minHeight = 400.0
        }
    }
    override val root = stackpane {
        prefWidth = 750.0
        borderpane {
            addClass(Styles.paddedContainer)
            center = vbox {
                add(find<GeneralSettingsView>())
                add(find<GitSettingsView>())
            }
            bottom = buttonbar {
                button(t("cancel")) {
                    isCancelButton = true
                    action {
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


