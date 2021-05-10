package org.umlreviewer.import

import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.app.RefreshFilesExplorer
import org.umlreviewer.app.SettingsChanged
import org.umlreviewer.app.Styles
import org.umlreviewer.components.form.loadingOverlay
import org.umlreviewer.utils.Helpers.getWorkingDirectory
import org.umlreviewer.utils.getUserHomeDirectory
import org.umlreviewer.utils.isWithinOrEqual
import org.umlreviewer.utils.t
import tornadofx.*
import java.io.File
import java.nio.file.Path

class SimpleImportView : View() {
    private val model: SimpleImportModel by inject()
    private val controller: ImportController by inject()
    private var fileChooserInitialDirectory = getUserHomeDirectory()
    private var workingDirectory = getWorkingDirectory()?.toFile()

    private val taskStatus = TaskStatus()
    private val extensionFilters = arrayOf(
        FileChooser.ExtensionFilter(
            t("pdfExtensionFilterDescription"),
            "*.pdf", "*.PDF"
        )
    )

    init {
        title = t("title")
        subscribe<SettingsChanged> {
            workingDirectory = getWorkingDirectory()?.toFile()
        }
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.minWidth = 600.0
            it.minHeight = 400.0
        }
        modalStage?.setOnCloseRequest {
            if (this.taskStatus.running.value) it.consume()
        }
    }


    override val root = stackpane {
        borderpane {
            disableProperty().bind(taskStatus.running)
            center = form {
                fieldset {
                    fitToParentHeight()
                    field(t("destination")){
                        hbox {
                            textfield(model.destinationString) {
                                hgrow = Priority.ALWAYS
                                isEditable = false
                                validator {
                                    if (model.destination.value == null)
                                        error(t("error.required"))
                                    else if (!model.destination.value.isWithinOrEqual(workingDirectory))
                                        error(t("error.destinationInWorkingDir"))
                                    else
                                        null
                                }
                            }
                            button("", FontIcon(FontAwesomeSolid.FOLDER_OPEN)) {
                                tooltip = tooltip(t("selectDestinationTooltip"))
                                action {
                                    val selectedDirectory = chooseDirectory(t("selectDestinationTooltip"), getInitialDirectory())
                                    if (selectedDirectory != null) {
                                        model.destination.value = selectedDirectory
                                        model.destinationString.value = selectedDirectory.absolutePath
                                    }
                                }
                            }
                        }
                    }

                    field {
                        fitToParentHeight()
                        vbox {
                            fitToParentHeight()
                            hbox {
                                style { padding = box(0.px, 0.px, 5.px, 0.px) }
                                hbox {
                                    alignment = Pos.BOTTOM_LEFT
                                    hgrow = Priority.ALWAYS
                                    label(t("files"))
                                }
                                button(t("addFiles"), graphic = FontIcon(FontAwesomeSolid.PLUS)) {
                                    action {
                                        val selectedFiles = chooseFile(
                                            title = t("addFilesTitle"),
                                            filters = extensionFilters,
                                            initialDirectory = fileChooserInitialDirectory,
                                            mode = FileChooserMode.Multi
                                        )
                                        if (selectedFiles.isNotEmpty()) {
                                            fileChooserInitialDirectory = selectedFiles.first().parentFile
                                            val tmp = model.files.map { it }.toMutableList()
                                            tmp.addAll(selectedFiles)
                                            model.files.clear()
                                            model.files.addAll(tmp.distinct())
                                        }
                                    }
                                }
                            }
                            vbox {
                                fitToParentHeight()
                                dynamicContent(model.files) { files: ObservableList<File>? ->
                                    if (files != null && files.size > 0) {
                                        listview(model.files) {
                                            fitToParentHeight()
                                            multiSelect(false)
                                            val lw = this
                                            cellFormat {
                                                val lc = this
                                                graphic = gridpane {
                                                    row {
                                                        hbox {
                                                            gridpaneColumnConstraints {
                                                                isFillWidth = true
                                                                hgrow = Priority.ALWAYS
                                                            }
                                                            label(it.name) {
                                                                minWidth = 0.0
                                                                maxWidthProperty().bind(lw.widthProperty()
                                                                    .minus(50.0).minus(lc.paddingLeft)
                                                                    .minus(lc.paddingRight))
                                                                tooltip = tooltip {
                                                                    text = it.absolutePath
                                                                    maxWidth = 350.0
                                                                    isWrapText = true
                                                                }
                                                            }
                                                        }
                                                        button("", FontIcon(FontAwesomeRegular.TRASH_ALT)) {
                                                            minWidth = 22.75
                                                            maxWidth = 22.75
                                                            prefWidth = 22.75
                                                            style {
                                                                fontSize = 10.px
                                                            }
                                                            action {
                                                                model.files.remove(it)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        textarea(t("noFilesSelected")) {
                                            isDisable = true
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }

            bottom = buttonbar {
                addClass(Styles.paddedContainer)

                button(t("cancel")) {
                    enableWhen { taskStatus.running.not() }
                    action {
                        close()
                    }
                }

                button(t("import")) {
                    enableWhen { model.valid
                        .and(model.files.isNotEqualTo(mutableListOf<File>().asObservable()))
                        .and(taskStatus.running.not())
                    }
                    action {
                        model.commit()
                        val item = model.item
                        runAsync(taskStatus) {
                            controller.importAndExtractDiagrams(item.destination!!, item.files)
                        } fail {
                            error(
                                title = t("error.defaultTitle"),
                                header = t("error.header"),
                                content = t("error.content", it.message)
                            )
                        } finally {
                            fire(RefreshFilesExplorer)
                            close()
                        }
                    }
                }
            }

            model.validate(decorateErrors = false)
        }
        loadingOverlay(taskStatus)
    }

    private fun getInitialDirectory(): File? {
        if (model.destination.value != null)
            return model.destination.value
        return workingDirectory
    }
}


fun openSimpleImportDialog(dest: Path? = null, paths: List<Path> = mutableListOf()) {
    val scope = Scope()
    val model = SimpleImportModel(DestinationAndFiles(
        dest = dest?.toFile(),
        files = paths.map { it.toFile() }.toMutableList()
    ))
    setInScope(model, scope)
    find(SimpleImportView::class, scope).openModal()
}
