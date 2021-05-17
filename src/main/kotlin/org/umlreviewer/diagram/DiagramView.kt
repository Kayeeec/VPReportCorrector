package org.umlreviewer.diagram

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Tooltip
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.icepdf.ri.common.MyAnnotationCallback
import org.icepdf.ri.common.SwingViewBuilder
import org.icepdf.ri.util.FontPropertiesManager
import org.icepdf.ri.util.ViewerPropertiesManager
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.ResizeEditorEvent
import org.umlreviewer.styles.Styles
import org.umlreviewer.components.form.loadingOverlay
import org.umlreviewer.diagram.DiagramViewConstants.DEFAULT_PDF_VIEWER_ICON_SIZE
import org.umlreviewer.diagram.components.CustomSwingNode
import org.umlreviewer.diagram.components.DiagramErrorsDrawerView
import org.umlreviewer.diagram.components.PdfViewerComponents
import org.umlreviewer.utils.t
import tornadofx.*
import java.awt.Dimension
import javax.swing.SwingUtilities

class DiagramView : View() {
    private val vm: DiagramViewModel by inject()
    private val diagramErrorsDrawer = find<DiagramErrorsDrawerView>(scope)

    private var swingController = vm.item.swingController
    private val swingNode = CustomSwingNode()
    private var viewerComponents: PdfViewerComponents by singleAssign()
    private val currentPageProperty = swingController.currentPageProperty

    private val centerView = vbox {
        fitToParentSize()
        widthProperty().onChange {
            resizeViewerPanel()
        }
        heightProperty().onChange {
            resizeViewerPanel()
        }
    }
    private var bottomBar: HBox? = null

    private val diagramErrorsBtn = togglebutton("", null) {
        addClass(Styles.flatButton)
        isSelected = false
        graphic = FontIcon(FontAwesomeSolid.TAGS)
        tooltip = Tooltip("Show errors the diagram contains")
    }
    private val toggleEditViewModeBtn = button {
        textProperty().bind(vm.editToggleBtnLabel)
        graphicProperty().bind(vm.editToggleBtnGraphic)
        addClass(Styles.flatButton)
        tooltip = tooltip {
            textProperty().bind(vm.editToggleBtnTooltip)
        }
        action {
            vm.toggleViewEditMode()
        }
    }
    private val saveBtn = button(t("save"), FontIcon(FontAwesomeSolid.SAVE)) {
        addClass(Styles.flatButton)
        visibleWhen { vm.isEditingProperty }
        tooltip = Tooltip("Save (Ctrl+S)")
        action {
            vm.save()
        }
        shortcut("Ctrl+S")
    }

    private var toolBarFlowPane: FlowPane by singleAssign()
    private var toolBar: HBox by singleAssign()

    init {
        subscribe<ResizeEditorEvent> {
            resizeViewerPanel(it.width, it.height)
        }
        createViewer()
        vm.openDocument {
            addPageNavigationIfNecessary()
        }
        swingNode.onMouseEntered = EventHandler {
            if (!swingNode.isFocused) {
                swingNode.requestFocus()
            }
        }
        vm.isEditingProperty.onChange {
            rebuildToolbar()
        }

    }

    private fun addPageNavigationIfNecessary() {
        if (swingController.document.numberOfPages > 1) {
            bottomBar = hbox {
                hgrow = Priority.ALWAYS
                alignment = Pos.BASELINE_CENTER
                button("", FontIcon(FontAwesomeSolid.FAST_BACKWARD)) {
                    addClass(Styles.flatButton)
                    enableWhen { swingController.previousAndFirstPageButtonEnabled }
                    action {
                        SwingUtilities.invokeLater { swingController.goToFirstPage() }
                    }
                }
                button("", FontIcon(FontAwesomeSolid.STEP_BACKWARD)) {
                    addClass(Styles.flatButton)
                    enableWhen { swingController.previousAndFirstPageButtonEnabled }
                    action {
                        SwingUtilities.invokeLater { swingController.goToPreviousPage() }
                    }
                }
                label(currentPageProperty) {
                    style { padding = box(0.px, 0.px, 0.px, 5.px) }
                }
                label("/${swingController.document.numberOfPages}") {
                    style { padding = box(0.px, 5.px, 0.px, 0.px) }
                }
                button("", FontIcon(FontAwesomeSolid.STEP_FORWARD)) {
                    addClass(Styles.flatButton)
                    enableWhen { swingController.nextAndLastPageButtonEnabled }
                    action {
                        SwingUtilities.invokeLater { swingController.goToNextPage() }
                    }
                }
                button("", FontIcon(FontAwesomeSolid.FAST_FORWARD)) {
                    addClass(Styles.flatButton)
                    enableWhen { swingController.nextAndLastPageButtonEnabled }
                    action {
                        SwingUtilities.invokeLater { swingController.goToLastPage() }
                    }
                }
            }
        }
    }

    private fun resizeViewerPanel(){
        resizeViewerPanel(centerView.widthProperty().value, centerView.heightProperty().value)
    }

    private fun resizeViewerPanel(width: Double, height: Double){
        val dimension = Dimension(width.toInt(), height.toInt())
        swingNode.resize(width, height)
        SwingUtilities.invokeLater {
            vm.viewerPanel.size = dimension
            vm.viewerPanel.minimumSize = dimension
            vm.viewerPanel.preferredSize = dimension
            vm.viewerPanel.maximumSize = dimension
            vm.viewerPanel.repaint()
        }
    }

    private fun createViewer() {
        SwingUtilities.invokeAndWait {
            try {
                vm.startLoading()

                swingController.setIsEmbeddedComponent(true)
                FontPropertiesManager.getInstance().loadOrReadSystemFonts()

                val properties = ViewerPropertiesManager.getInstance()
                setPdfViewerPreferences(properties)

                swingController.documentViewController.annotationCallback =
                    MyAnnotationCallback(swingController.documentViewController)

                val factory = SwingViewBuilder(swingController, properties)
                vm.viewerPanel = factory.buildUtilityAndDocumentSplitPane(false)
                viewerComponents = PdfViewerComponents(factory)
                toolBar = getInitialToolbar()
                vm.viewerPanel.revalidate()
                swingNode.content = vm.viewerPanel
                centerView.add(swingNode)
            } catch (e: Exception) {
                log.severe(e.stackTraceToString())
            } finally {
                vm.endLoading()
            }
        }
    }

    private fun getInitialToolbar(): HBox {
        return hbox {
            style {
                padding = box(4.px)
            }
            hgrow = Priority.ALWAYS
            toolBarFlowPane = flowpane {
                hgrow = Priority.ALWAYS
                add(viewerComponents.showHideUtilityPane)
                add(toggleEditViewModeBtn)
                add(viewerComponents.fitPage)
                add(viewerComponents.pan)
                add(viewerComponents.textSelecion)
            }
            add(diagramErrorsBtn)
        }
    }

    private fun rebuildToolbar() {
        if (vm.isEditingProperty.value == true) {
            with(toolBarFlowPane) {
                addChildIfPossible(saveBtn, 2)
                separator()
                viewerComponents.annotationButtons.forEach { add(it) }
            }
        } else {
            viewerComponents.annotationButtons.forEach { it.removeFromParent() }
            saveBtn.removeFromParent()
        }
    }

    private fun setPdfViewerPreferences(properties: ViewerPropertiesManager) {
        properties.clearPreferences()
        properties.setFloat(ViewerPropertiesManager.PROPERTY_DEFAULT_ZOOM_LEVEL, 1.25f)
        properties.set(ViewerPropertiesManager.PROPERTY_ICON_DEFAULT_SIZE, DEFAULT_PDF_VIEWER_ICON_SIZE)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ZOOM, false)

        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITY_OPEN, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITY_SAVE, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITY_PRINT, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITY_UPANE, true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITY_SEARCH, false)

        // only show annotations card in utility pane
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_SEARCH, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION, true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION_DESTINATIONS, true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION_FLAGS, true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_ATTACHMENTS, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_BOOKMARKS, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_SIGNATURES, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_THUMBNAILS, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_UTILITYPANE_LAYERS, false)

        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_STATUSBAR, false)

        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_FIT, true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ROTATE, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_TOOL, true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_FORMS, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_SEARCH, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_FULL_SCREEN, false)

        // enable only certain quick annotation buttons
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION, true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_ARROW     , true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_CIRCLE    , true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_FREE_TEXT , true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_HIGHLIGHT , false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_INK       , true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_LINE      , true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_LINK      , false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_PERMISSION, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_PREVIEW   , false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_RECTANGLE , true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_SELECTION , true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_STRIKE_OUT, false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_TEXT      , true)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_UNDERLINE , false)
        properties.setBoolean(ViewerPropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION_UTILITY   , false)
    }

    override val root = stackpane {
        fitToParentSize()
        addClass(Styles.diagramAnnotatorView)
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS
        borderpane {
            fitToParentSize()
            top = toolBar
            center = centerView
            centerView.fitToParentSize()
            right = diagramErrorsDrawer.root
            diagramErrorsDrawer.drawerExpandedProperty.bind(diagramErrorsBtn.selectedProperty())
            bottom = bottomBar
        }

        loadingOverlay {
            visibleWhen { vm.isLoading }
        }
    }
}
