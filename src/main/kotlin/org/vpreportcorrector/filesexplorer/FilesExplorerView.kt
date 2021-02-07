package org.vpreportcorrector.filesexplorer

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.fontawesome.FontAwesome
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.RefreshFilesExplorer
import org.vpreportcorrector.app.Styles.Companion.centered
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.mainview.GlobalDataModel
import org.vpreportcorrector.settings.SettingsModalView
import org.vpreportcorrector.utils.list
import org.vpreportcorrector.utils.p
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FilesExplorerView : View() {
    private val controller: FilesExplorerController by inject()
    private val globalDataModel: GlobalDataModel by inject(FX.defaultScope)
    private val rootFolderProp = SimpleObjectProperty<Path?>(globalDataModel.workingDirectory.value?.toPath())
    private lateinit var filesTree: TreeView<Path>
    private val pasteStatus = TaskStatus()
    private val deleteStatus = TaskStatus()

    init {
        globalDataModel.workingDirectory.onChange { file: File? ->
            rootFolderProp.value = file?.toPath()
        }
        subscribe<RefreshFilesExplorer> {
            refreshTreeView()
        }
    }

    override val root = stackpane {
        dynamicContent(rootFolderProp) { rootFolder: Path? ->
            if (rootFolder != null) {
                vbox {
                    fitToParentSize()
                    hbox {
                        hgrow = Priority.ALWAYS
                        alignment = Pos.CENTER_RIGHT

                        button("", FontIcon(FontAwesome.REPEAT)) {
                            disableWhen { rootFolderProp.isNull }
                            tooltip("Refresh")
                            action {
                                refreshTreeView()
                            }
                        }
                        button("", FontIcon(FontAwesomeSolid.FOLDER_PLUS)){
                            disableWhen { rootFolderProp.isNull }
                            tooltip("New folder (in the directory root)")
                            action {
                                controller.createFolder(location = rootFolderProp.value!!.toFile())
                                refreshTreeView()
                            }
                        }
                        button("", FontIcon(BootstrapIcons.ARROWS_EXPAND)){
                            disableWhen { rootFolderProp.isNull }
                            tooltip("Expand all")
                            action { filesTree.root.expandAll() }
                        }
                        button("", FontIcon(BootstrapIcons.ARROWS_COLLAPSE)){
                            disableWhen { rootFolderProp.isNull }
                            tooltip("Collapse all")
                            action { filesTree.root.collapseAll() }
                        }
                    }

                    filesTree = treeview(TreeItem(rootFolder)) {
                        minWidth = 50.0
                        isEditable = true
                        populate {
                            if (Files.isDirectory(it.value)) it.value.list()
                            else null
                        }
                        cellFormat {
                            text = "${it.fileName}"
                            val file = it.toFile()
                            graphic = when {
                                file.isDirectory -> FontIcon(FontAwesomeSolid.FOLDER)
                                controller.pdfExtensions.contains(file.extension) -> FontIcon(FontAwesome.FILE_PDF_O)
                                controller.imageExtensions.contains(file.extension) -> FontIcon(FontAwesome.FILE_IMAGE_O)
                                else -> FontIcon(FontAwesome.FILE)
                            }
                        }

                        multiSelect(true)

                        contextmenu {
                            item("New folder") {
                                visibleWhen { controller.model.isNewFolderVisible }
                                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isNewFolderVisible) }
                                action {
                                    val location = selectionModel.selectedItems.firstOrNull()?.value?.toFile()
                                    if (location != null ) {
                                        try {
                                            controller.createFolder(location = location)
                                        } finally {
                                            refreshTreeView()
                                        }
                                    }
                                }
                            }
                            item("Import") {
                                visibleWhen { controller.model.isImportVisible }
                                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isImportVisible) }
                                action {
                                    controller.import(selectionModel.selectedItems.toList())
                                }
                            }
                            item("Edit") {
                                visibleWhen { controller.model.isEditVisible }
                                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isEditVisible) }
                                action {
                                    // TODO: 07.02.21
                                }
                            }
                            item("View") {
                                visibleWhen { controller.model.isViewVisible }
                                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isViewVisible) }
                                action {
                                    // TODO: 07.02.21
                                }
                            }
                            item("Rename") {
                                visibleWhen { controller.model.isRenameVisible }
                                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isRenameVisible) }
                                action {
                                    if (selectionModel.selectedItems !== null
                                        && selectionModel.selectedItems.isNotEmpty()
                                        && selectionModel.selectedItems.size == 1
                                    ) {
                                        val toRename = selectionModel.selectedItems.firstOrNull()?.value
                                        if (toRename != null) {
                                            try {
                                                controller.rename(toRename)
                                            } finally {
                                                refreshTreeView()
                                            }
                                        }
                                    }
                                }
                            }
                            separator()
                            item("Copy", KeyCombination.keyCombination("Shortcut+C")){
                                enableWhen { controller.model.isFileTreeFocused }
                                action {
                                    if (selectionModel.selectedItems !== null && selectionModel.selectedItems.isNotEmpty()) {
                                        val pathsToCopy = selectionModel.selectedItems.map { it.value }
                                        controller.clipboardCopy(pathsToCopy)
                                    }
                                }
                            }
                            item("Paste", KeyCombination.keyCombination("Shortcut+V")) {
                                action {
                                    runAsync(pasteStatus) {
                                        controller.clipboardPaste(selectionModel.selectedItems.first().value)
                                    } fail {
                                        log.severe(it.stackTraceToString())
                                        error(
                                            title = "Error",
                                            header = "Error occurred while pasting files from clipboard.",
                                            content = "Error message:\n${it.message}"
                                        )
                                    } finally {
                                        refreshTreeView()
                                    }
                                }
                            }
                            item("Delete", KeyCombination.keyCombination("Delete")) {
                                enableWhen { controller.model.isFileTreeFocused }
                                action {
                                    if (selectionModel.selectedItems !== null && selectionModel.selectedItems.isNotEmpty()) {
                                        val pathsToDelete = selectionModel.selectedItems.map { it.value }
                                        val header = "Do you really want to delete selected item/s?"
                                        confirm(header = header, title= "Confirm deletion", actionFn = {
                                            runAsync(deleteStatus) {
                                                controller.delete(pathsToDelete)
                                            } finally {
                                                refreshTreeView()
                                            }
                                        })
                                    }
                                }
                            }
                        }
                        root.isExpanded = true

                        selectionModel.selectedItems.onChange {
                            controller.onSelectedItemsChange(selectionModel)
                        }

                        focusedProperty().onChange { newValue: Boolean ->
                            controller.model.isFileTreeFocused.value = newValue
                        }
                    }
                    filesTree.fitToParentSize()
                }
            } else {
                alignment = Pos.CENTER
                vbox {
                    addClass(centered)
                    alignment = Pos.CENTER
                    p("No working directory selected. Set it via 'Files -> Settings'.")
                    hyperlink("Open settings").action { find<SettingsModalView>().openModal() }
                }
            }
        }

        loadingOverlay {
            visibleWhen { pasteStatus.running.or(deleteStatus.running) }
        }
    }

    private fun refreshTreeView() {
        with(filesTree){
            refreshTreeItemRecursively(this.root)
        }
    }

    private fun refreshTreeItemRecursively(root: TreeItem<Path>){
        if (root.value.toFile().isDirectory) {
            val subPathsSet = root.value.list().toSet()
            val toAdd: List<Path> = subPathsSet.filter { path -> !root.children.map { it.value }.toSet().contains(path) }
            val toRemove: List<TreeItem<Path>> = root.children.filter { treeItem -> !subPathsSet.contains(treeItem.value) }
            root.children.removeAll(toRemove)
            root.children.addAll(toAdd.map { TreeItem(it) })
            root.children.forEach { refreshTreeItemRecursively(it) }
        }
    }
}
