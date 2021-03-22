package org.vpreportcorrector.diagram

import javafx.event.EventHandler
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import org.icepdf.ri.common.MyAnnotationCallback
import org.icepdf.ri.common.SwingViewBuilder
import org.icepdf.ri.util.FontPropertiesManager
import org.icepdf.ri.util.ViewerPropertiesManager
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.app.ResizeEditorEvent
import org.vpreportcorrector.app.Styles
import org.vpreportcorrector.components.form.loadingOverlay
import org.vpreportcorrector.diagram.components.CustomSwingNode
import org.vpreportcorrector.diagram.components.DEFAULT_PDF_VIEWER_ICON_SIZE
import org.vpreportcorrector.diagram.components.DiagramErrorsDrawerView
import org.vpreportcorrector.diagram.components.PdfViewerSwingButtons
import tornadofx.*
import java.awt.Dimension
import javax.swing.SwingUtilities

// TODO: add conditional pager
// TODO: refactor - MVVM/MVC
class DiagramView : View() {
    private val controller: DiagramController by inject()
    private val diagramErrorsDrawer = find<DiagramErrorsDrawerView>(scope)

    private var swingController = controller.swingController
    private val swingNode = CustomSwingNode()
    private var swingButtons: PdfViewerSwingButtons by singleAssign()

    private val centerView = vbox {
        fitToParentSize()
        widthProperty().onChange {
            resizeViewerPanel()
        }
        heightProperty().onChange {
            resizeViewerPanel()
        }
    }

    private val diagramErrorsBtn = togglebutton("", null) {
        addClass(Styles.flatButton)
        isSelected = false
        graphic = FontIcon(FontAwesomeSolid.TAGS)
        tooltip = Tooltip("Show errors the diagram contains")
    }
    private val switchToEditBtn = button("Edit", FontIcon(FontAwesomeSolid.EDIT)) {
        addClass(Styles.flatButton)
        tooltip = Tooltip("Switch to edit mode")
        hiddenWhen { controller.model.isEditingProperty }
        action {
            controller.switchToEditMode()
        }
    }
    private val saveBtn = button("", FontIcon(FontAwesomeSolid.SAVE)) {
        addClass(Styles.flatButton)
        visibleWhen { controller.model.isEditingProperty }
        tooltip = Tooltip("Save")
        action {
            controller.save()
        }
    }

    private var toolBar = hbox {
        style {
            padding = box(4.px)
        }
        hgrow = Priority.ALWAYS
    }

    init {
        subscribe<ResizeEditorEvent> {
            resizeViewerPanel(it.width, it.height)
        }
        createViewerAndOpenDocument()
        controller.openDocument()
        controller.loadData()
        swingNode.onMouseEntered = EventHandler {
            if (!swingNode.isFocused) {
                swingNode.requestFocus()
            }
        }
        controller.model.isEditingProperty.onChangeOnce {
            buildToolbar()
        }
    }

    private fun resizeViewerPanel(){
        resizeViewerPanel(centerView.widthProperty().value, centerView.heightProperty().value)
    }

    private fun resizeViewerPanel(width: Double, height: Double){
        val dimension = Dimension(width.toInt(), height.toInt())
        swingNode.resize(width, height)
        SwingUtilities.invokeLater {
            controller.viewerPanel.size = dimension
            controller.viewerPanel.minimumSize = dimension
            controller.viewerPanel.preferredSize = dimension
            controller.viewerPanel.maximumSize = dimension
            controller.viewerPanel.repaint()
        }
    }

    private fun createViewerAndOpenDocument() {
        SwingUtilities.invokeAndWait {
            try {
                controller.model.loadingLatch.startLoading()

                swingController.setIsEmbeddedComponent(true)
                FontPropertiesManager.getInstance().loadOrReadSystemFonts()

                val properties = ViewerPropertiesManager.getInstance()
                setPdfViewerPreferences(properties)

                swingController.documentViewController.annotationCallback =
                    MyAnnotationCallback(swingController.documentViewController)

                val factory = SwingViewBuilder(swingController, properties)
                controller.viewerPanel = factory.buildUtilityAndDocumentSplitPane(false)
                swingButtons = PdfViewerSwingButtons(factory)
                buildToolbar()
                controller.viewerPanel.revalidate()
                swingNode.content = controller.viewerPanel
                centerView.add(swingNode)
            } catch (e: Exception) {
                log.severe(e.stackTraceToString())
            } finally {
                controller.model.loadingLatch.endLoading()
            }
        }
    }

    private fun buildToolbar() {
        toolBar.children.clear()
        if (controller.model.isEditingProperty.value == true) {
            buildEditorToolbar()
        } else {
            buildViewerToolbar()
        }
    }

    private fun buildViewerToolbar() {
        with(toolBar) {
            flowpane {
                add(swingButtons.showHideUtilityPane)
                add(swingButtons.fitPage)
                add(swingButtons.pan)
                add(swingButtons.textSelecion)
            }

            hbox { hgrow = Priority.ALWAYS }

            add(switchToEditBtn)
            add(diagramErrorsBtn)
        }
    }

    private fun buildEditorToolbar() {
        with(toolBar) {
            flowpane {
                add(swingButtons.showHideUtilityPane)
                add(saveBtn)
                add(swingButtons.fitPage)
                add(swingButtons.pan)
                add(swingButtons.textSelecion)
                separator()
                add(swingButtons.selectAnnotation)
                add(swingButtons.lineAnnotation)
                add(swingButtons.lineArrowAnnotation)

                add(swingButtons.squareAnnotation)
                add(swingButtons.circleAnnotation)
                add(swingButtons.inkAnnotation)

                add(swingButtons.freeTextAnnotation)
                add(swingButtons.textAnnotation)
            }

            hbox { hgrow = Priority.ALWAYS }

            add(diagramErrorsBtn)
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
        }

        loadingOverlay {
            visibleWhen { controller.model.loadingLatch.isLoading }
        }
    }
}
