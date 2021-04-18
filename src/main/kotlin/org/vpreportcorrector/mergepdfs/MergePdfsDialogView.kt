package org.vpreportcorrector.mergepdfs

import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.RefreshFilesExplorer
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.utils.Helpers.getWorkingDirectory
import org.vpreportcorrector.utils.isPdf
import org.vpreportcorrector.utils.isWriteable
import org.vpreportcorrector.utils.t
import tornadofx.*
import java.io.File

class MergePdfsDialogView : View() {
    private val vm: MergePdfsDialogViewModel by inject()
    private var workingDirectory = getWorkingDirectory()?.toFile()
    private val extensionFilters = arrayOf(
        FileChooser.ExtensionFilter(
            t("pdfExtensionFilterDescription"),
            "*.pdf", "*.PDF"
        )
    )
    private var filesListView: ListView<File> by singleAssign()

    private val MERGED_FILE_NAME = "merged_report.pdf"

    init {
        title = t("title")
        subscribe<SettingsChanged> { workingDirectory = getWorkingDirectory()?.toFile() }
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.minWidth = 600.0
            it.minHeight = 300.0
        }
        modalStage?.setOnCloseRequest {
            if (vm.mergeTaskStatus.running.value) it.consume()
        }
        vm.addValidator(
            node = filesListView,
            property = vm.pdfFilesProperty,
            validator = {
                if (it == null || it.size < 2)
                    error(t("error.minFileCount"))
                else
                    null
            }
        )
    }

    override val root = stackpane {
        borderpane {
            addClass(Styles.paddedContainer)
            center = form {
                fieldset(labelPosition = Orientation.VERTICAL) {
                    field(t("pdfsLabel"), Orientation.VERTICAL) {
                        hbox {
                            filesListView = listview(vm.pdfFilesProperty) {
                                hgrow = Priority.ALWAYS
                                multiSelect(true)
                                cellFormat {
                                    text = if (workingDirectory != null) it.toRelativeString(workingDirectory!!) else it.absolutePath
                                }
                                vm.selectionModelProperty.bindBidirectional(selectionModelProperty())
                            }
                            vbox {
                                alignment = Pos.TOP_CENTER
                                spacing = 5.0
                                style {
                                    padding = box(0.px, (-5).px, 0.px, 10.px)
                                }
                                button("", FontIcon(FontAwesomeSolid.PLUS))  {
                                    tooltip = Tooltip(t("addPdf"))
                                    action {
                                        val toAdd = chooseFile(
                                            title = t("addPdfsToMerge"),
                                            filters = extensionFilters,
                                            mode = FileChooserMode.Multi
                                        ) {
                                            initialDirectory = workingDirectory
                                        }
                                        vm.add(toAdd)
                                    }
                                }
                                button("", FontIcon(FontAwesomeSolid.TRASH))  {
                                    tooltip = Tooltip(t("remove"))
                                    enableWhen { vm.isDeleteEnabled}
                                    action {
                                        vm.delete()
                                    }
                                }
                                button("", FontIcon(FontAwesomeSolid.ARROW_UP)) {
                                    tooltip = Tooltip(t("moveUp"))
                                    enableWhen { vm.isMoveUpEnabled }
                                    action {
                                        vm.moveUp()
                                    }
                                }
                                button("", FontIcon(FontAwesomeSolid.ARROW_DOWN))  {
                                    tooltip = Tooltip(t("moveDown"))
                                    enableWhen { vm.isMoveDownEnabled }
                                    action {
                                        vm.moveDown()
                                    }
                                }
                            }
                        }
                    }
                    field(t("destinationFileLabel"), Orientation.VERTICAL) {
                        hbox {
                            textfield(vm.destinationFileStringProperty) {
                                hgrow = Priority.ALWAYS
                                isEditable = false
                                validator {
                                    if (vm.destinationFileProperty.value == null)
                                        this.error(t("error.destFile.required"))
                                    else if (!isPdf(vm.destinationFileProperty.value))
                                        this.error(t("error.destFile.notPdf"))
                                    else if (!vm.destinationFileProperty.value.isWriteable())
                                        this.error(t("error.destFile.notWriteable"))
                                    else
                                        null
                                }
                            }
                            button("", FontIcon(FontAwesomeSolid.FOLDER_OPEN)) {
                                action {
                                    val files = chooseFile(
                                        title = t("saveAs"),
                                        filters = extensionFilters,
                                        mode = FileChooserMode.Save,
                                    ) {
                                        initialFileName = MERGED_FILE_NAME
                                        initialDirectory = workingDirectory
                                    }
                                    files.firstOrNull()?.let {
                                        vm.setDestination(it)
                                    }
                                    vm.validationContext.validate(vm.destinationFileStringProperty)
                                }
                            }
                        }
                    }
                }
            }
            bottom = buttonbar {
                button(t("cancel")) {
                    isCancelButton = true
                    action {
                        close()
                    }
                }
                button(t("merge")) {
                    isDefaultButton = true
                    enableWhen { vm.valid }
                    action {
                        vm.commit {
                            runAsync(vm.mergeTaskStatus) {
                                updateMessage(t("mergingTaskMsg"))
                                vm.merge()
                            } fail {
                                log.severe(it.stackTraceToString())
                                error(
                                    title = t("error.defaultTitle"),
                                    header = t("error.header"),
                                    content = t("error.content", it.message)
                                )
                            } success {
                                fire(RefreshFilesExplorer)
                                close()
                            }
                        }
                    }
                }
            }
            vm.validate(decorateErrors = false)
        }
        loadingOverlay(vm.mergeTaskStatus)
    }
}

