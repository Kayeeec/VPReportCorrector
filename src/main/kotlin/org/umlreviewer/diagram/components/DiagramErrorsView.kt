package org.umlreviewer.diagram.components

import javafx.scene.control.ScrollPane
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.app.Styles
import org.umlreviewer.components.collapseAll
import org.umlreviewer.components.expandAll
import org.umlreviewer.diagram.*
import org.umlreviewer.enums.DiagramIssueGroup
import org.umlreviewer.utils.AppConstants.SCROLL_BAR_WIDTH
import org.umlreviewer.utils.t
import tornadofx.*

class DiagramErrorsView : View() {
    private val dvm: DiagramViewModel by inject()

    private var squeezeBox: SqueezeBox by singleAssign()

    override val root = borderpane {
        addClass(Styles.diagramErrorsView)
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
        prefWidth = 400.0
        fitToParentSize()
        top = hbox {
            addClass(Stylesheet.title)
            hbox {
                addClass(Stylesheet.title)
                hgrow = Priority.ALWAYS
                label(t("title"))
            }
            button {
                addClass(Styles.flatButton)
                graphic = FontIcon(BootstrapIcons.ARROWS_EXPAND)
                tooltip = tooltip(t("expandAll"))
                action { squeezeBox.expandAll() }
            }
            button {
                addClass(Styles.flatButton)
                graphic = FontIcon(BootstrapIcons.ARROWS_COLLAPSE)
                tooltip = tooltip(t("collapseAll"))
                action { squeezeBox.collapseAll() }
            }
        }
        center = scrollpane {
            fitToParentSize()
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
            prefWidth = this@borderpane.prefWidth
            squeezeBox = squeezebox {
                prefWidthProperty().bind(this@scrollpane.widthProperty().minus(SCROLL_BAR_WIDTH))
                usePrefWidth = true
                for(group in enumValues<DiagramIssueGroup>()) {
                    fold(t(group.name), true) {
                        tooltip = tooltip(text) {
                            isWrapText = true
                            prefWidth = 300.0
                        }
                        useMaxWidth = true
                        fitToParentWidth()
                        vbox {
                            useMaxWidth = true
                            hgrow = Priority.ALWAYS
                            for (issue in group.issues) {
                                checkbox(t(issue.name)) {
                                    enableWhen { dvm.isEditingProperty }
                                    isSelected = dvm.diagramIssuesProperty.contains(issue)
                                    tooltip = tooltip(text) {
                                        isWrapText = true
                                        prefWidth = 300.0
                                        showDelay = Duration.millis(300.0)
                                    }
                                    isWrapText = true
                                    prefWidth = this@borderpane.prefWidth - SCROLL_BAR_WIDTH - 2
                                    usePrefWidth = true
                                    action {
                                        dvm.updateDiagramIssue(issue, isSelected)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

