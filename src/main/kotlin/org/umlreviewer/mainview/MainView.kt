package org.umlreviewer.mainview

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.app.RequestSync
import org.umlreviewer.app.SettingsChanged
import org.umlreviewer.app.Styles.Companion.flatButton
import org.umlreviewer.app.Styles.Companion.paddedContainer
import org.umlreviewer.components.form.loadingOverlay
import org.umlreviewer.import.openSimpleImportDialog
import org.umlreviewer.mainview.content.ContentViewModel
import org.umlreviewer.settings.modal.SettingsModalView
import org.umlreviewer.statistics.StatisticsView
import org.umlreviewer.sync.SyncController
import org.umlreviewer.utils.Helpers.getWorkingDirectory
import org.umlreviewer.utils.t
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

