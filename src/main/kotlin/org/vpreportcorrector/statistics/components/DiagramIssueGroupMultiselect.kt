package org.vpreportcorrector.statistics.components

import javafx.beans.property.SimpleSetProperty
import javafx.scene.control.CheckBoxTreeItem
import org.controlsfx.control.CheckTreeView
import org.vpreportcorrector.enums.DiagramIssueGroup
import org.vpreportcorrector.utils.t
import tornadofx.Fragment
import tornadofx.onChange

class DiagramIssueGroupMultiselect : Fragment(), RefreshableInputComponent {
    val diagramIssueGroups: SimpleSetProperty<DiagramIssueGroup> by param()
    val treeRootLabel: String by param("Diagram issue groups")

    private fun getCheckboxTree(): CheckTreeView<CustomTreeItem> {
        val root = CheckBoxTreeItem<CustomTreeItem>(CustomTreeItem(null, treeRootLabel))
        root.isExpanded = true
        val groups = DiagramIssueGroup.values().map { group ->
            val treeItem = CheckBoxTreeItem<CustomTreeItem>(
                CustomTreeItem(group, t(group.name)),
                null,
                diagramIssueGroups.contains(group)
            )
            treeItem.selectedProperty().onChange { selected ->
                if (selected) diagramIssueGroups.add(group)
                else diagramIssueGroups.remove(group)
            }
            treeItem
        }
        root.children.addAll(groups)
        return CheckTreeView(root)
    }

    private val tree = getCheckboxTree()
    override val root = tree

    private fun recursivelyRefreshTree(treeItem: CheckBoxTreeItem<CustomTreeItem>?) {
        if (treeItem == null) return
        val doSelect = treeItem.value.value != null && diagramIssueGroups.contains(treeItem.value.value!! as DiagramIssueGroup)
        if (doSelect) tree.checkModel.check(treeItem)
        treeItem.children.forEach {
            recursivelyRefreshTree(it as CheckBoxTreeItem<CustomTreeItem>?)
        }
    }

    override fun refreshInputs() {
        tree.checkModel.clearChecks()
        recursivelyRefreshTree(tree.root as CheckBoxTreeItem<CustomTreeItem>?)
    }
}

