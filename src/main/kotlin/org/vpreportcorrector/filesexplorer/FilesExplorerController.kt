package org.vpreportcorrector.filesexplorer

import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TreeItem
import javafx.stage.Modality
import org.apache.commons.io.FileUtils
import org.vpreportcorrector.app.errorhandling.ErrorCollector
import org.vpreportcorrector.filesexplorer.dialogs.*
import org.vpreportcorrector.utils.*
import tornadofx.Controller
import tornadofx.Scope
import tornadofx.find
import tornadofx.putFiles
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths


class FilesExplorerController: Controller() {
    val pdfExtensions = setOf("pdf", "PDF")
    val imageExtensions = setOf(
        "jpg", "png", "bmp", "jpeg", "svg",
        "JPG", "PNG", "BMP", "JPEG", "SVG",
    )
    val model: FilesExplorerModel by inject()

    fun getRootFolder(): Path? {
        var result: Path? = null
        try {
            var workingDirectoryPath: String = ""
            preferences {
                sync()
                workingDirectoryPath = get(KEY_WORKING_DIRECTORY, "")
            }
            if (workingDirectoryPath.isNotBlank()) result = Paths.get(workingDirectoryPath)
        } catch (e: Exception) {
            log.severe(e.stackTraceToString())
            tornadofx.error(
                title = "Error",
                header = "Error occurred while getting working directory.",
                content = "Error message:\n${e.message}"
            )
        }
        return result
    }

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
                && selectionModel.selectedItems.all {
                    val file = it.value.toFile()
                    !file.isDirectory && pdfExtensions.contains(file.extension)
                }
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
        errorCollector.verify()
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

    private fun checkConflictsAndCopyFileOrDir(rememberedAction: RememberChoice, copied: Path, targetDir: File): RememberChoice {
        val (conflictingFile, result) = checkConflicts(targetDir, copied, rememberedAction)
        var remChoice = rememberedAction
        if (conflictingFile != null) {
            remChoice = resolveRememberedAction(rememberedAction, result, copied)
            val chosen = remChoice.getRelevantChoice(copied)
            if (chosen == FileConflictChoice.REPLACE_OR_MERGE
                || result?.choice == FileConflictChoice.REPLACE_OR_MERGE) {
                remChoice = replaceFileOrMergeDirectory(copied, targetDir, conflictingFile, remChoice)
            }
            else if (chosen == FileConflictChoice.RENAME || result?.choice == FileConflictChoice.RENAME){
                val newName = result?.newName ?: suggestName(targetDir.toPath(), copied)
                copyUsingNewName(copied, targetDir, newName)
            }
        } else {
            copyFileOrDirectory(copied, targetDir)
        }
        return remChoice
    }

    private fun replaceFileOrMergeDirectory(
        copied: Path,
        targetDir: File,
        conflictingFile: Path,
        rememberedAction: RememberChoice
    ): RememberChoice {
        var remAction = rememberedAction
        if (copied.toFile().isDirectory) {
            copied.list().forEach { path ->
                remAction = checkConflictsAndCopyFileOrDir(remAction, path, conflictingFile.toFile())
            }
        } else {
            if (!Files.isSameFile(copied, conflictingFile)) {
                FileUtils.copyFileToDirectory(copied.toFile(), targetDir)
            }
        }
        return remAction
    }

    /**
     * non conflicting
     */
    private fun copyFileOrDirectory(copied: Path, targetDir: File) {
        if (copied.toFile().isDirectory) {
            FileUtils.copyDirectoryToDirectory(copied.toFile(), targetDir)
        } else {
            FileUtils.copyFileToDirectory(copied.toFile(), targetDir)
        }
    }

    private fun copyUsingNewName(copied: Path, targetDir: File, newName: String) {
        if (copied.toFile().isDirectory) {
            val newDir = File(targetDir, newName)
            newDir.mkdirs()
            FileUtils.copyDirectory(copied.toFile(), newDir)
        } else {
            val newFile = File(targetDir, newName)
            FileUtils.copyFile(copied.toFile(), newFile)
        }
    }

    private fun resolveRememberedAction(
        rememberedAction: RememberChoice,
        result: FileConflictResult?,
        copied: Path,
    ): RememberChoice {
        if (copied.toFile().isDirectory) {
            if (rememberedAction.directory == null && result != null && result.remember) {
                rememberedAction.directory = result.choice
                return rememberedAction
            }
            return rememberedAction
        } else {
            if (rememberedAction.file == null && result != null && result.remember) {
                rememberedAction.file = result.choice
                return rememberedAction
            }
            return rememberedAction
        }
    }

    private fun checkConflicts(targetDir: File, path: Path, rememberedAction: RememberChoice): Pair<Path?, FileConflictResult?> {
        val conflictingFile = findConflictingFile(targetDir.toPath(), path)
        var result: FileConflictResult? = null

        if (conflictingFile != null && rememberedAction.getRelevantChoice(path) == null) {
            result = openFileExistsDialog(conflictingFile, path)
        }
        return Pair(conflictingFile, result)
    }

    private fun openFileExistsDialog(existing: Path, copied: Path): FileConflictResult {
        val scope = Scope()
        val model = FileExistsDialogModel(FileExistsDialog(existing, copied))
        setInScope(model, scope)
        find<FileExistsDialogView>(scope).openModal(
            block = true,
            modality = Modality.WINDOW_MODAL,
            resizable = false,
            escapeClosesWindow = false,
        )
        return FileConflictResult(model)
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

    fun clipboardPaste(targetPath: Path){
        val files = mutableListOf<File>()
        try {
            clipboard.files?.let { files.addAll(it) }
        } catch (e: Exception) {
            log.severe(e.stackTraceToString())
            tornadofx.error(
                title = "Error",
                header = "Error occurred while pasting files from clipboard.",
                content = "Error message:\n${e.message}"
            )
        }
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
                errorCollector.addError(
                    "Error occurred copying file ${path.toAbsolutePath()} to destination: ${targetDir.absolutePath}",
                    e
                )
            }
        }
        errorCollector.verify()
    }
}

































