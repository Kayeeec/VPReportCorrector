package org.umlreviewer.components

import javafx.geometry.Pos
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Priority
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.styles.Styles
import org.umlreviewer.styles.Styles.Companion.treeToolbar
import org.umlreviewer.utils.t
import tornadofx.*


fun <T> TreeView<T>.expandAll(skipRoot: Boolean = false) {
    recursivelySetExpanded(this.root, true)
}

fun <T> TreeView<T>.collapseAll(skipRoot: Boolean = false) {
    recursivelySetExpanded(this.root, false, skipRoot)
}

fun <T> recursivelySetExpanded(root: TreeItem<T>?, isExpanded: Boolean, isRootToSkip: Boolean = false) {
    if (root == null) return
    if (!isRootToSkip) root.isExpanded = isExpanded
    if (root.isLeaf) return
    root.children.forEach { recursivelySetExpanded(it, isExpanded) }
}

fun <T> UIComponent.getTreeTopBar(tree: TreeView<T>, labelText: String = "") = hbox {
    alignment = Pos.CENTER_RIGHT
    addClass(Stylesheet.title)
    addClass(treeToolbar)
    hbox {
        addClass(Stylesheet.title)
        hgrow = Priority.ALWAYS
        label(labelText)
    }
    button {
        addClass(Styles.flatButton)
        graphic = FontIcon(BootstrapIcons.ARROWS_EXPAND)
        tooltip = tooltip(t("expandAll"))
        action { tree.expandAll() }
    }
    button {
        addClass(Styles.flatButton)
        graphic = FontIcon(BootstrapIcons.ARROWS_COLLAPSE)
        tooltip = tooltip(t("collapseAll"))
        action { tree.collapseAll() }
    }
}
