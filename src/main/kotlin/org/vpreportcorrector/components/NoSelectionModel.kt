package org.vpreportcorrector.components

import javafx.scene.control.MultipleSelectionModel
import javafx.collections.FXCollections

import javafx.collections.ObservableList

/**
 * To disable selection in ListView.
 * Also necessary to disable focusTraversable.
 * Used in DiagramErrorsView.
 *
 * @see https://stackoverflow.com/a/46186195/7677851
 */
class NoSelectionModel<T> : MultipleSelectionModel<T>() {
    override fun getSelectedIndices(): ObservableList<Int> {
        return FXCollections.emptyObservableList()
    }

    override fun getSelectedItems(): ObservableList<T> {
        return FXCollections.emptyObservableList()
    }

    override fun selectIndices(index: Int, vararg indices: Int) {}
    override fun selectAll() {}
    override fun selectFirst() {}
    override fun selectLast() {}
    override fun clearAndSelect(index: Int) {}
    override fun select(index: Int) {}
    override fun select(obj: T) {}
    override fun clearSelection(index: Int) {}
    override fun clearSelection() {}
    override fun isSelected(index: Int): Boolean {
        return false
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun selectPrevious() {}
    override fun selectNext() {}
}
