package org.vpreportcorrector.mainview

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.RequestSync
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.app.Styles.Companion.flatButton
import org.vpreportcorrector.app.Styles.Companion.paddedContainer
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.import.openSimpleImportDialog
import org.vpreportcorrector.mainview.content.ContentViewModel
import org.vpreportcorrector.settings.modal.SettingsModalView
import org.vpreportcorrector.statistics.StatisticsView
import org.vpreportcorrector.sync.SyncController
import org.vpreportcorrector.utils.Helpers.getWorkingDirectory
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.nio.file.Path

// TODO KB: extract viewmodel?
class MainView : View() {
    private val workingDirectory = SimpleObjectProperty<Path>(getWorkingDirectory())
    private val contentViewModel: ContentViewModel by inject()
    private val syncController = find<SyncController>()

    private val selectedContentViewType = SimpleObjectProperty(ContentViewType.EDITOR)

    init {
        title = t("appName")
        subscribe<SettingsChanged> {
            workingDirectory.value = getWorkingDirectory()
        }
    }

    private val contentBorderPane = find<EditorView>()

    override val root = borderpane {
        top = hbox {
            disableWhen { syncController.isAnyTaskRunning }
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER_LEFT

            menubar {
                removeClass(paddedContainer)
                style {
                    padding = box(0.px, 10.px, 0.px, 0.px)
                }
                menu(t("file")) {
                    item(t("settings"), "Shortcut+Alt+S", FontIcon(FontAwesomeSolid.COGS)) {
                        action {
                            find<SettingsModalView>().openModal()
                        }
                    }
                    item(t("import"), "Shortcut+Alt+I", FontIcon(FontAwesomeSolid.FILE_IMPORT)) {
                        enableWhen { workingDirectory.isNotNull }
                        action {
                            openSimpleImportDialog()
                        }
                    }
                    separator()
                    item(t("quit"), "Shortcut+Q", FontIcon(FontAwesomeSolid.POWER_OFF)) {
                        action {
                            if (contentViewModel.checkUnsavedChanges()) {
                                Platform.exit()
                            }
                        }
                    }
                }
            }
            togglegroup {
                bind(selectedContentViewType)
                togglebutton(t(ContentViewType.EDITOR.name), value = ContentViewType.EDITOR) {
                    addClass(flatButton)
                    fitToParentHeight()
                    graphic = FontIcon(FontAwesomeSolid.EDIT)
                    action {
                        isSelected = true
                        find<StatisticsView>().replaceWith<EditorView>(
                            ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.RIGHT)
                        )
                    }
                }
                togglebutton(t(ContentViewType.STATISTICS.name), value = ContentViewType.STATISTICS) {
                    addClass(flatButton)
                    graphic = FontIcon(FontAwesomeSolid.CHART_BAR)
                    fitToParentHeight()
                    action {
                        isSelected = true
                        find<EditorView>().replaceWith<StatisticsView>(
                            ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.LEFT)
                        )
                    }
                }
            }
            hbox { hgrow = Priority.ALWAYS }
            button("Sync", FontIcon(FontAwesomeSolid.SYNC_ALT)) {
                addClass(flatButton)
                tooltip = tooltip("Synchronise with remote repository")
                disableWhen { syncController.isAnyTaskRunning.or(syncController.isSyncServiceInitialized.not()) }
                action { fire(RequestSync) }
            }
        }

        center = stackpane {
            add(contentBorderPane)

            loadingOverlay(syncController.syncTaskStatus)
            loadingOverlay(syncController.initTaskStatus)
        }
    }
}

