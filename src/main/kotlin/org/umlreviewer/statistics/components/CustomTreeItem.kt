package org.umlreviewer.statistics.components

data class CustomTreeItem(val value: Any?, val label: String) {
    override fun toString(): String {
        return label
    }
}
