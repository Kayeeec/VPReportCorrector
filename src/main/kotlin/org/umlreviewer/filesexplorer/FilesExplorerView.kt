package org.umlreviewer.filesexplorer

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseButton
import javafx.scene.layout.Priority
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.fontawesome.FontAwesome
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.OpenDiagramEvent
import org.umlreviewer.OpenDiagramInNewWindowEvent
import org.umlreviewer.RefreshFilesExplorer
import org.umlreviewer.SettingsChanged
import org.umlreviewer.styles.Styles.Companion.centered
import org.umlreviewer.styles.Styles.Companion.flatButton
import org.umlreviewer.components.form.loadingOverlay
import org.umlreviewer.settings.modal.SettingsModalView
import org.umlreviewer.utils.*
import org.umlreviewer.utils.file.isImage
import org.umlreviewer.utils.file.isPdf
import org.umlreviewer.utils.file.list
import tornadofx.*
import java.nio.file.Files
import java.nio.file.Path

// TODO: 08.03.21 test shortcuts
class FilesExplorerView : View() {
    private val controller: FilesExplorerController by inject()
    private val rootFolderProp = SimpleObjectProperty<Path?>(this.getWorkingDirectory())
    private lateinit var filesTree: TreeView<Path>
    private val pasteStatus = TaskStatus()
    private val deleteStatus = TaskStatus()

    init {
        subscribe<SettingsChanged> {
            val newWorkDir = getWorkingDirectory()
            if (newWorkDir != rootFolderProp.value) {
                rootFolderProp.value = newWorkDir
            }
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
                            addClass(flatButton)
                            disableWhen { rootFolderProp.isNull }
                            tooltip("Refresh")
                            action {
                                refreshTreeView()
                            }
                        }
                        button("", FontIcon(FontAwesomeSolid.FOLDER_PLUS)){
                            addClass(flatButton)
                            disableWhen { rootFolderProp.isNull }
                            tooltip("New folder (in the directory root)")
                            action {
                                controller.createFolder(location = rootFolderProp.value!!.toFile())
                                refreshTreeView()
                            }
                        }
                        button("", FontIcon(BootstrapIcons.ARROWS_EXPAND)){
                            addClass(flatButton)
                            disableWhen { rootFolderProp.isNull }
                            tooltip(t("expandAll"))
                            action { filesTree.root.expandAll() }
                        }
                        button("", FontIcon(BootstrapIcons.ARROWS_COLLAPSE)){
                            addClass(flatButton)
                            disableWhen { rootFolderProp.isNull }
                            tooltip(t("collapseAll"))
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
                                isPdf(file) -> FontIcon(FontAwesome.FILE_PDF_O)
                                isImage(file) -> FontIcon(FontAwesome.FILE_IMAGE_O)
                                else -> FontIcon(FontAwesome.FILE)
                            }
                            onMouseClicked = EventHandler { mouseClick ->
                                if (mouseClick.clickCount == 2) {
                                    openDiagram()
                                }
                                else if (mouseClick.button == MouseButton.MIDDLE
                                    || (mouseClick.isAltDown && mouseClick.button == MouseButton.PRIMARY)) {
                                    openDiagram(true)
                                }
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
                            item("Open") {
                                visibleWhen { controller.model.isEditVisible }
                                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isEditVisible) }
                                action {
                                    openDiagram()
                                }
                            }
                            item("Open in new window") {
                                visibleWhen { controller.model.isEditVisible }
                                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isEditVisible) }
                                action {
                                    openDiagram(true)
                                }
                            }
                            item("Merge PDFs...") {
                                visibleWhen { controller.model.isMergePdfsVisible }
                                enableWhen { controller.model.isFileTreeFocused.and(controller.model.isMergePdfsVisible) }
                                action {
                                    controller.openMergePdfsDialog(selectionModel)
                                }
                            }
                            separator()
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
                            item("Copy", KeyCombination.keyCombination("Shortcut+C")){
                                enableWhen { controller.model.isFileTreeFocused }
                                action {
                                    if (filesTree.isFocused
                                        && selectionModel.selectedItems !== null
                                        && selectionModel.selectedItems.isNotEmpty()) {
                                        val pathsToCopy = selectionModel.selectedItems.map { it.value }
                                        controller.clipboardCopy(pathsToCopy)
                                    }
                                }
                            }
                            item("Paste", KeyCombination.keyCombination("Shortcut+V")) {
                                action {
                                    if (filesTree.isFocused) {
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
                            }
                            item("Delete", KeyCombination.keyCombination("Delete")) {
                                enableWhen { controller.model.isFileTreeFocused }
                                action {
                                    if (filesTree.isFocused
                                        && selectionModel.selectedItems !== null
                                        && selectionModel.selectedItems.isNotEmpty()) {
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

    private fun openDiagram(inNewWindow: Boolean = false) {
        if (filesTree.selectionModel.selectedItems.size == 1 && controller.model.isEditVisible.value) {
            if (inNewWindow) {
                fire(OpenDiagramInNewWindowEvent(filesTree.selectionModel.selectedItems[0].value) )
            } else {
                fire(OpenDiagramEvent(filesTree.selectionModel.selectedItems[0].value))
            }
        }
    }
}
