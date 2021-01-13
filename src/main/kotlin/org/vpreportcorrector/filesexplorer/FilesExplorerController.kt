package org.vpreportcorrector.filesexplorer

import javafx.scene.control.MultipleSelectionModel
import javafx.scene.control.TreeItem
import tornadofx.Controller
import java.io.File
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
}

