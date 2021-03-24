package org.vpreportcorrector.mainview

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.layout.Priority
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.app.Styles.Companion.flatButton
import org.vpreportcorrector.app.Styles.Companion.paddedContainer
import org.vpreportcorrector.app.Styles.Companion.sideButton
import org.vpreportcorrector.filesexplorer.FilesExplorerView
import org.vpreportcorrector.import.openSimpleImportDialog
import org.vpreportcorrector.mainview.content.ContentView
import org.vpreportcorrector.mainview.content.ContentViewModel
import org.vpreportcorrector.settings.SettingsModalView
import org.vpreportcorrector.utils.getWorkingDirectory
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.nio.file.Path

class MainView : View() {
    private val workingDirectory = SimpleObjectProperty<Path>(getWorkingDirectory())
    private val filesExplorerView: FilesExplorerView by inject()
    private val contentView: ContentView by inject()
    private val contentViewModel: ContentViewModel by inject()

    private var filesExplorerVisible = true
    private var filesExplorerDividerPosition: Double = 0.25
    private var filesExplorerNode: Node? = null

    init {
        title = t("appName")
        subscribe<SettingsChanged> {
            workingDirectory.value = getWorkingDirectory()
        }
    }

    private val centerSplitPane = splitpane {
        orientation = Orientation.HORIZONTAL
        setDividerPositions(filesExplorerDividerPosition)
        add(filesExplorerView)
        add(contentView)
        SplitPane.setResizableWithParent(this.items[0], false)
    }

    override val root = borderpane {
        top = hbox {
            hgrow = Priority.ALWAYS
            menubar {
                removeClass(paddedContainer)
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
        }
        left = vbox {
            group {
                togglebutton(t("directory")) {
                    action {
                        toggleFilesExplorerPane()
                    }
                    isSelected = filesExplorerVisible
                    graphic = FontIcon(FontAwesomeSolid.FOLDER)
                    rotate = -90.0
                    addClass(sideButton, flatButton)
                    tooltip(t("directoryTooltip"))
                }
            }
        }
        center = centerSplitPane
    }

    private fun toggleFilesExplorerPane() {
        if (filesExplorerVisible) {
            filesExplorerNode = centerSplitPane.items[0]
            filesExplorerDividerPosition = centerSplitPane.dividerPositions[0]
            centerSplitPane.items.removeAt(0)
            filesExplorerVisible = false
        } else if (filesExplorerNode !== null) {
            centerSplitPane.items.add(0, filesExplorerNode)
            centerSplitPane.setDividerPosition(0, filesExplorerDividerPosition)
            filesExplorerVisible = true
        }
    }
}
