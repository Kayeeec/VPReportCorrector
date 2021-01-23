package org.vpreportcorrector.filesexplorer

import javafx.geometry.Pos
import javafx.scene.control.TreeItem
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.fontawesome.FontAwesome
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.utils.list
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Path

class FilesExplorerView : View("Working directory") {
    private val controller: FilesExplorerController by inject()
    private val rootFolder = controller.getRootFolder()

    private var filesTree = treeview<Path>(TreeItem(rootFolder)) {
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
                        controller.createFolder(location = location, newFolderName = null)
                        refreshTreeView()
                    }
                }
            }
            item("Import") {
                visibleWhen { controller.model.isImportVisible }
                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isImportVisible) }
            }
            item("Edit") {
                visibleWhen { controller.model.isEditVisible }
                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isEditVisible) }
            }
            item("View") {
                visibleWhen { controller.model.isViewVisible }
                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isViewVisible) }
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
                            controller.rename(toRename)
                            refreshTreeView()
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
                        controller.copy(pathsToCopy)
                    }
                }
            }
            item("Paste", KeyCombination.keyCombination("Shortcut+V")) {
                enableWhen { controller.model.isPasteEnabled }
                action {
                    try {
                        controller.paste(selectionModel.selectedItems.first().value)
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
                            controller.delete(pathsToDelete)
                            refreshTreeView()
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

    override val root = vbox {
        hbox {
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER_RIGHT

            button("", FontIcon(FontAwesome.REPEAT)) {
                tooltip("Refresh")
                action {
                    refreshTreeView()
                }
            }
            button("", FontIcon(FontAwesomeSolid.FOLDER_PLUS)){
                tooltip("New folder")
                action {
                    controller.createFolder(location = rootFolder.toFile(), newFolderName = null)
                    refreshTreeView()
                }
            }
            button("", FontIcon(BootstrapIcons.ARROWS_EXPAND)){
                tooltip("Expand all")
                action { filesTree.root.expandAll() }
            }
            button("", FontIcon(BootstrapIcons.ARROWS_COLLAPSE)){
                tooltip("Collapse all")
                action { filesTree.root.collapseAll() }
            }
        }

        add(filesTree)
        filesTree.fitToParentHeight()
        filesTree.fitToParentWidth()
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
