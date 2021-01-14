package org.vpreportcorrector.filesexplorer

import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TreeItem
import org.vpreportcorrector.app.errorhandling.ErrorCollector
import tornadofx.Controller
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths

// TODO: 07.01.21 add settings for repository location
const val TEST_REPO_LOCATION = "/mnt/shared/Skola/DP/testRepositoryRoot"

class FilesExplorerController: Controller() {
    val pdfExtensions = setOf("pdf", "PDF")
    val imageExtensions = setOf(
        "jpg", "png", "bmp", "jpeg", "svg",
        "JPG", "PNG", "BMP", "JPEG", "SVG",
    )
    val model: FilesExplorerModel by inject()

    fun getRootFolder(): Path {
        return Paths.get(TEST_REPO_LOCATION)
    }

    fun recomputeContextMenuVisibilities(selectionModel: MultipleSelectionModel<TreeItem<Path>>?) {
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

    fun delete(pathsToDelete: List<Path>) {
        val errorCollector = ErrorCollector("Error/s occurred while deleting file/s.")
        pathsToDelete.forEach {
            try {
                deleteDirectoryStream(it)
            } catch (e: NoSuchFileException) {
                // this can be ignored
            } catch (e: IOException) {
                e.printStackTrace()
                errorCollector.addError("Unexpected error has occurred during IO operation: ${e.message}", e)
            } catch (e: SecurityException) {
                e.printStackTrace()
                errorCollector.addError("Insufficient permissions to delete file: ${e.message}", e)
            } catch (e: Exception) {
                e.printStackTrace()
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
}

