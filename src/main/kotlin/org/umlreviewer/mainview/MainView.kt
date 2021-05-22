package org.umlreviewer.mainview

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.RequestSync
import org.umlreviewer.SettingsChanged
import org.umlreviewer.components.form.loadingOverlay
import org.umlreviewer.import.openSimpleImportDialog
import org.umlreviewer.mainview.content.ContentViewModel
import org.umlreviewer.settings.modal.SettingsModalView
import org.umlreviewer.statistics.StatisticsView
import org.umlreviewer.styles.AppColors
import org.umlreviewer.styles.Styles.Companion.flatButton
import org.umlreviewer.styles.Styles.Companion.paddedContainer
import org.umlreviewer.sync.SyncController
import org.umlreviewer.utils.PreferencesHelper.getWorkingDirectory
import org.umlreviewer.utils.Shortcuts
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

    private fun openSettings() {
        find<SettingsModalView>().openModal()
    }

    private fun closeApp() {
        confirm("Do you really want to quit ${t("appName")}?", title = "Quit ${t("appName")}") {
            if (contentViewModel.checkUnsavedChanges()) {
                Platform.exit()
            }
        }
    }

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
                    item(t("settings"), Shortcuts.SETTINGS.name, FontIcon(FontAwesomeSolid.COGS)) {
                        action { openSettings() }
                    }
                    item(t("import"), Shortcuts.IMPORT.name, FontIcon(FontAwesomeSolid.FILE_IMPORT)) {
                        enableWhen { workingDirectory.isNotNull }
                        action { openSimpleImportDialog() }
                    }
                    separator()
                    item(t("quit"), Shortcuts.QUIT.name, FontIcon(FontAwesomeSolid.POWER_OFF)) {
                        action { closeApp() }
                    }
                }
            }
            button("", FontIcon(FontAwesomeSolid.COGS)) {
                addClass(flatButton)
                tooltip = tooltip("${t("settings")} (${Shortcuts.SETTINGS.displayText})") {
                    showDelay = Duration.millis(200.0)
                }
                action { openSettings() }
            }
            button("", FontIcon(FontAwesomeSolid.FILE_IMPORT)) {
                addClass(flatButton)
                tooltip = tooltip("${t("import")} (${Shortcuts.IMPORT.displayText})") {
                    showDelay = Duration.millis(200.0)
                }
                action { openSimpleImportDialog() }
            }
            hbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.CENTER

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
                        disableWhen { workingDirectory.isNull }
                        action {
                            isSelected = true
                            find<EditorView>().replaceWith<StatisticsView>(
                                ViewTransition.Slide(0.3.seconds, ViewTransition.Direction.LEFT)
                            )
                        }
                    }
                }
            }
            button("Sync", FontIcon(FontAwesomeSolid.SYNC_ALT)) {
                addClass(flatButton)
                tooltip = tooltip("Synchronise with remote repository") {
                    showDelay = Duration.millis(200.0)
                }
                disableWhen { syncController.isAnyTaskRunning.or(syncController.isSyncServiceInitialized.not()) }
                action { fire(RequestSync) }
            }
            button("", FontIcon(FontAwesomeSolid.POWER_OFF).apply { iconColor = AppColors.textError }) {
                addClass(flatButton)
                tooltip = tooltip("${t("quit")} (${Shortcuts.QUIT.displayText})") {
                    showDelay = Duration.millis(200.0)
                }
                action { closeApp() }
            }
        }

        center = stackpane {
            add(contentBorderPane)

            loadingOverlay(syncController.syncTaskStatus)
            loadingOverlay(syncController.initTaskStatus)
        }
    }
}

