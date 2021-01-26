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
import org.vpreportcorrector.app.SettingsChanged
import org.vpreportcorrector.app.Styles.Companion.centered
import org.vpreportcorrector.settings.SettingsModalView
import org.vpreportcorrector.utils.p
import org.vpreportcorrector.utils.list
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Path

class FilesExplorerView : View("Working directory") {
    private val controller: FilesExplorerController by inject()
    private val rootFolderProp = SimpleObjectProperty<Path?>(controller.getRootFolder())
    private lateinit var filesTree: TreeView<Path>

    init {
        subscribe<SettingsChanged> {
            val newRoot = controller.getRootFolder()
            if (rootFolderProp.value != newRoot) {
                rootFolderProp.value = newRoot
            }
        }
    }

    override val root = vbox {
        dynamicContent(rootFolderProp) { rootFolder: Path? ->
            if (rootFolder != null) {
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

                filesTree = treeview<Path>(TreeItem(rootFolder)) {
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
                                    controller.createFolder(location = location)
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
                                    controller.clipboardCopy(pathsToCopy)
                                }
                            }
                        }
                        item("Paste", KeyCombination.keyCombination("Shortcut+V")) {
                            action {
                                try {
                                    controller.clipboardPaste(selectionModel.selectedItems.first().value)
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
                                        try {
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
                filesTree.fitToParentHeight()
                filesTree.fitToParentWidth()
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
