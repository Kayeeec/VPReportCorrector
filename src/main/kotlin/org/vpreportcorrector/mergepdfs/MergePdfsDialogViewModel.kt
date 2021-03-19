package org.vpreportcorrector.mergepdfs

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.MultipleSelectionModel
import tornadofx.*
import java.io.File

class MergePdfsDialogViewModel(files: List<File>): ItemViewModel<MergePdfsModel>(MergePdfsModel(pdfFiles = files)) {
    val pdfFilesProperty = bind(MergePdfsModel::pdfFilesProperty)
    val destinationFileProperty = bind(MergePdfsModel::destinationFileProperty)

    val destinationFileStringProperty = bind { SimpleStringProperty("") }
    val selectionModelProperty = SimpleObjectProperty<MultipleSelectionModel<File>>()
    val mergeTaskStatus = TaskStatus()

    val isDeleteEnabled = SimpleBooleanProperty(false)
    val isMoveUpEnabled = SimpleBooleanProperty(false)
    val isMoveDownEnabled = SimpleBooleanProperty(false)

    init {
        selectionModelProperty.onChangeOnce { sm ->
            sm?.let {
                bindSelectionListeners()
            }
        }
    }

    private fun bindSelectionListeners() {
        if (selectionModelProperty.value == null) return
        selectionModelProperty.value.selectedIndices.onChange {
            selectionModelProperty.value.let { sm ->
                val indices = sm.selectedIndices
                val someSelected = indices.size > 0
                val isSequence = isSequence(indices)
                isDeleteEnabled.value = someSelected
                isMoveUpEnabled.value = someSelected && !(isSequence && indices.contains(0))
                isMoveDownEnabled.value = someSelected && !(isSequence && indices.contains(pdfFilesProperty.value.lastIndex))
            }
        }
    }

    fun setDestination(file: File) {
        destinationFileProperty.value = file
        destinationFileStringProperty.value = file.absolutePath
    }

    fun delete() {
        val files = selectionModelProperty.value.selectedItems.toList()
        pdfFilesProperty.removeAll(files)
    }

    fun moveUp() {
        move(-1)
    }

    fun moveDown() {
        move(1)
    }

    private fun move(step: Int) {
        val limit = if (step < 0) 0 else pdfFilesProperty.value.lastIndex
        val sortedIds = if (step < 0) selectionModelProperty.value.selectedIndices.toList().sorted()
            else selectionModelProperty.value.selectedIndices.toList().sortedDescending()
        val newIds = mutableSetOf<Int>()
        sortedIds.forEach { i ->
            val newId = i + step
            if (i != limit && !newIds.contains(newId)) {
                pdfFilesProperty.swap(newId, i)
                newIds.add(newId)
            } else { // if i == limit or next to selected item
                newIds.add(i)
            }
        }
        selectionModelProperty.value.clearSelection()
        newIds.forEach { selectionModelProperty.value.select(it) }
    }

    fun add(files: List<File>) {
        val filesSet = LinkedHashSet(pdfFilesProperty.value)
        val toAdd = files.distinct().filter { !filesSet.contains(it) }
        pdfFilesProperty.addAll(toAdd)
    }

    /**
     * Merges PDF files from [pdfFilesProperty].
     * @see [MergePdfsModel.merge]
     */
    fun merge() {
        item.merge()
    }

    private fun isSequence(numbers: List<Int>): Boolean {
        if(numbers.isEmpty()) return true
        val sorted = numbers.distinct().sorted()
        return sorted.last() - sorted.first() + 1 == sorted.size
    }
}
