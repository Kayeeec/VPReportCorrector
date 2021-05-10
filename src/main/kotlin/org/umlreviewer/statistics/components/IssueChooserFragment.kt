package org.umlreviewer.statistics.components

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.geometry.Pos
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import org.controlsfx.control.CheckTreeView
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.app.Styles
import org.umlreviewer.components.collapseAll
import org.umlreviewer.components.expandAll
import org.umlreviewer.enums.DiagramIssue
import org.umlreviewer.enums.DiagramIssueGroup
import org.umlreviewer.statistics.enums.IssueChooserMode
import org.umlreviewer.utils.t
import tornadofx.*

class IssueChooserFragment : Fragment("Select issues or issue groups"), RefreshableInputComponent {
    val issueSelectMode: SimpleObjectProperty<IssueChooserMode> by param()
    val issueGroups: SimpleSetProperty<DiagramIssueGroup> by param()
    val issues: SimpleSetProperty<DiagramIssue> by param()

    private var singleIssueTree = getSingleIssueTree()
    private var singleIssueChooser = getSingleIssueChooser()
    private var issueGroupChooser = find<DiagramIssueGroupMultiselect>(mapOf(
        DiagramIssueGroupMultiselect::diagramIssueGroups to issueGroups,
    ))

    private fun getSingleIssueChooser(): BorderPane {
        return borderpane {
            fitToParentSize()
            hgrow = Priority.ALWAYS
            top = hbox {
                alignment = Pos.CENTER_RIGHT
                button {
                    addClass(Styles.flatButton)
                    graphic = FontIcon(BootstrapIcons.ARROWS_EXPAND)
                    tooltip = tooltip(t("expandAll"))
                    action { singleIssueTree.expandAll(true) }
                }
                button {
                    addClass(Styles.flatButton)
                    graphic = FontIcon(BootstrapIcons.ARROWS_COLLAPSE)
                    tooltip = tooltip(t("collapseAll"))
                    action { singleIssueTree.collapseAll(true) }
                }
            }
            center = singleIssueTree
        }
    }

    private fun getSingleIssueTree(): CheckTreeView<IssueTreeItem> {
        val root = CheckBoxTreeItem<IssueTreeItem>(IssueTreeItem(null, "Issues"))
        val groups = DiagramIssueGroup.values().map { group ->
            val groupTreeItem = CheckBoxTreeItem(IssueTreeItem(null, t(group.name)))
            val issueTreeItems = group.issues.map { issue ->
                val issueTreeItem = CheckBoxTreeItem(IssueTreeItem(issue, t(issue.name)))
                issueTreeItem.isExpanded = true
                issueTreeItem.isSelected = issues.contains(issue)
                issueTreeItem.selectedProperty().onChange { selected ->
                    if (selected) {
                        issues.add(issue)
                    } else {
                        issues.remove(issue)
                    }
                }
                issueTreeItem
            }
            groupTreeItem.children.addAll(issueTreeItems)
            groupTreeItem.isExpanded = true
            groupTreeItem
        }
        root.isExpanded = true
        root.children.addAll(groups)

        val tree = CheckTreeView(root)
        tree.isShowRoot = false
        return tree
    }

    private fun refreshSingleIssueTree() {
        recursivelyRefreshTree(singleIssueTree.root as CheckBoxTreeItem<IssueTreeItem>?)
    }

    private fun recursivelyRefreshTree(treeItem: CheckBoxTreeItem<IssueTreeItem>?) {
        if (treeItem == null) return
        treeItem.isSelected = treeItem.value.value != null && issues.contains(treeItem.value.value!!)
        treeItem.children.forEach {
            recursivelyRefreshTree(it as CheckBoxTreeItem<IssueTreeItem>?)
        }
    }

    override val root = form {
        addClass(Styles.issueChooserFragment)
        fieldset("Select issues or issue groups") {
            field {
                vbox {
                    togglegroup {
                        bind(issueSelectMode)
                        radiobutton("Choose individual issues", value = IssueChooserMode.SINGLE_ISSUE) {
                            isSelected = issueSelectMode.value == IssueChooserMode.SINGLE_ISSUE
                        }
                        radiobutton(
                            "Choose issue groups (the Y axis will show issue groups instead of individual issues)",
                            value = IssueChooserMode.ISSUE_GROUP
                        ) {
                            isSelected = issueSelectMode.value == IssueChooserMode.ISSUE_GROUP
                        }
                    }
                }
            }
            field {
                vbox {
                    dynamicContent(issueSelectMode) {
                        if (issueSelectMode.value == IssueChooserMode.SINGLE_ISSUE) {
                            add(singleIssueChooser)
                        } else {
                            add(issueGroupChooser)
                        }
                    }
                }
            }
        }
    }

    override fun refreshInputs() {
        refreshSingleIssueTree()
        issueGroupChooser.refreshInputs()
    }
}

data class IssueTreeItem(val value: DiagramIssue?, val label: String ) {
    override fun toString(): String {
        return label
    }
}
