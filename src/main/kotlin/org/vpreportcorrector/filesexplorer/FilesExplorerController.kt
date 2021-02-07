package org.vpreportcorrector.filesexplorer

import javafx.application.Platform
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TreeItem
import javafx.stage.Modality
import org.vpreportcorrector.app.errorhandling.ErrorCollector
import org.vpreportcorrector.filesexplorer.dialogs.*
import org.vpreportcorrector.import.openSimpleImportDialog
import org.vpreportcorrector.utils.checkConflictsAndCopyFileOrDir
import org.vpreportcorrector.utils.isDescendantOf
import org.vpreportcorrector.utils.isPdf
import tornadofx.Controller
import tornadofx.Scope
import tornadofx.find
import tornadofx.putFiles
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.concurrent.FutureTask


class FilesExplorerController: Controller() {
    val pdfExtensions = setOf("pdf", "PDF")
    val imageExtensions = setOf(
        "jpg", "png", "bmp", "jpeg", "svg",
        "JPG", "PNG", "BMP", "JPEG", "SVG",
    )
    val model: FilesExplorerModel by inject()

    fun onSelectedItemsChange(selectionModel: MultipleSelectionModel<TreeItem<Path>>?) {
        recomputeContextMenuVisibilities(selectionModel)
    }

    private fun recomputeContextMenuVisibilities(selectionModel: MultipleSelectionModel<TreeItem<Path>>?) {
        recomputeIsImportVisible(selectionModel)
        recomputeIsVisibleAndIsEditable(selectionModel)
        recomputeIsRenameVisible(selectionModel)
        recomputeIsNewFolderVisible(selectionModel)
    }

    private fun recomputeIsVisibleAndIsEditable(selectionModel: MultipleSelectionModel<TreeItem<Path>>?) {
        val file: File? = selectionModel?.selectedItems?.firstOrNull()?.value?.toFile()
        val tmp = (selectionModel != null
                && selectionModel.selectedItems.size == 1
                && !file!!.isDirectory
                && imageExtensions.contains(file.extension))
        model.isViewVisible.value = tmp
        model.isEditVisible.value = tmp
    }

    private fun recomputeIsImportVisible(selectionModel: MultipleSelectionModel<TreeItem<Path>>?) {
        model.isImportVisible.value = selectionModel != null
                && !selectionModel.isEmpty
                && (
                    selectionModel.selectedItems.all {
                        val file = it.value.toFile()
                        !file.isDirectory && isPdf(file)
                    }
                    || (selectionModel.selectedItems.size == 1 && Files.isDirectory(selectionModel.selectedItems[0].value))
                )
    }

    private fun recomputeIsRenameVisible(selectionModel: MultipleSelectionModel<TreeItem<Path>>?) {
        model.isRenameVisible.value = selectionModel != null
                && selectionModel.selectedItems.size == 1
    }

    private fun recomputeIsNewFolderVisible(selectionModel: MultipleSelectionModel<TreeItem<Path>>?) {
        model.isNewFolderVisible.value = selectionModel != null
                && selectionModel.selectedItems.size == 1
                && selectionModel.selectedItems[0].value.toFile().isDirectory
    }

    fun rename(path: Path) {
        val scope = Scope()
        val model = RenameDialogModel(RenameDialog(path))
        setInScope(model, scope)
        find(RenameDialogView::class, scope).openModal(
            block = true,
            modality = Modality.WINDOW_MODAL,
            resizable = false,
        )
    }

    fun delete(pathsToDelete: List<Path>) {
        val errorCollector = ErrorCollector("Error/s occurred while deleting file/s.")
        pathsToDelete.forEach {
            try {
                deleteDirectoryStream(it)
            } catch (e: NoSuchFileException) {
                // this can be ignored
            } catch (e: IOException) {
                log.severe(e.stackTraceToString())
                errorCollector.addError("Unexpected error has occurred during IO operation: ${e.message}", e)
            } catch (e: SecurityException) {
                log.severe(e.stackTraceToString())
                errorCollector.addError("Insufficient permissions to delete file: ${e.message}", e)
            } catch (e: Exception) {
                log.severe(e.stackTraceToString())
                errorCollector.addError("Failed to delete file: ${e.message}", e)
            }
        }
        Platform.runLater { errorCollector.verify() }
    }

    private fun deleteDirectoryStream(path: Path) {
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .map { it.toFile() }.forEach { it.delete() }
    }

    private fun filterDuplicateSelections(pathsToCopy: List<Path>): List<Path> {
        val sortedPaths = pathsToCopy.sortedBy { it.toFile().absolutePath }
        val filtered = mutableListOf<Path>()
        sortedPaths.forEach { path ->
            if (filtered.none { path.isDescendantOf(it) }) {
                filtered.add(path)
            }
        }
        return filtered
    }

    private fun resolveTargetDir(targetPath: Path): File {
        var tmp: Path = targetPath
        if (!targetPath.toFile().isDirectory) {
            tmp = targetPath.parent
        }
        return tmp.toFile()
    }

    fun createFolder(location: File, newFolderName: String = "") {
        val scope = Scope()
        val model = NewFolderModel(NewFolder(location, newFolderName))
        setInScope(model, scope)
        find(NewFolderDialogView::class, scope).openModal(block = true)
    }

    fun clipboardCopy(paths: List<Path>) {
        try {
            val filteredFiles = filterDuplicateSelections(paths).map { it.toFile() }
            clipboard.putFiles(filteredFiles.toMutableList())
        } catch (e: Exception) {
            log.warning(e.stackTraceToString())
            tornadofx.error(
                title = "Error",
                header = "Error occurred while copying files to clipboard.",
                content = "Error message:\n${e.message}"
            )
        }
    }

    fun clipboardPaste(targetPath: Path) {
        val clipboardFilesTask = FutureTask {
            clipboard.files ?: listOf<File>()
        }
        Platform.runLater(clipboardFilesTask)
        val files = clipboardFilesTask.get()
        if (files.isNotEmpty()) {
            pasteFiles(targetPath, files)
        }
    }

    private fun pasteFiles(targetPath: Path, copiedFiles: List<File>) {
        val errorCollector = ErrorCollector("Error/s occurred while pasting:")
        var remembered = RememberChoice()
        val targetDir = resolveTargetDir(targetPath)
        copiedFiles.map { it.toPath() }.forEach { path ->
            try {
                remembered = checkConflictsAndCopyFileOrDir(remembered, path, targetDir)
            } catch (e: Exception) {
                log.severe(e.stackTraceToString())
                errorCollector.addError(
                    "Error occurred copying file ${path.toAbsolutePath()} to destination: ${targetDir.absolutePath}",
                    e
                )
            }
        }
        Platform.runLater { errorCollector.verify() }
    }

    fun import(selected: List<TreeItem<Path>>) {
        if (selected.size == 1 && Files.isDirectory(selected[0].value)) {
            openSimpleImportDialog(dest = selected[0].value)
        } else {
            val pdfFiles = selected.map { it.value }.filter { isPdf(it.toFile()) }
            openSimpleImportDialog(paths = pdfFiles)
        }
    }
}

































