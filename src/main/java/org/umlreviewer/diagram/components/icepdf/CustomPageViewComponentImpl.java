package org.umlreviewer.diagram.components.icepdf;

import org.icepdf.core.pobjects.PageTree;
import org.icepdf.ri.common.tools.*;
import org.icepdf.ri.common.views.DocumentViewModel;
import org.icepdf.ri.common.views.PageViewComponentImpl;

import java.util.ArrayList;

public class CustomPageViewComponentImpl extends PageViewComponentImpl {

    public CustomPageViewComponentImpl(DocumentViewModel documentViewModel, PageTree pageTree, int pageIndex, int width, int height) {
        super(documentViewModel, pageTree, pageIndex, width, height);
    }

    public void setToolMode(int viewToolMode) {
        if (this.currentToolHandler != null) {
            this.currentToolHandler.uninstallTool();
            this.removeMouseListener(this.currentToolHandler);
            this.removeMouseMotionListener(this.currentToolHandler);
            this.currentToolHandler = null;
        }

        switch(viewToolMode) {
            case 2:
                this.currentToolHandler = new ZoomInPageHandler(this.documentViewController, this);
                break;
            case 3:
            case 4:
            case 5:
            case 10:
            default:
                this.currentToolHandler = null;
                break;
            case 6:
                this.currentToolHandler = new AnnotationSelectionHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
                break;
            case 7:
                this.currentToolHandler = new LinkAnnotationHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
                break;
            case 8:
                this.currentToolHandler = new HighLightAnnotationHandler(this.documentViewController, this);
                ((HighLightAnnotationHandler)this.currentToolHandler).createTextMarkupAnnotation((ArrayList)null);
                this.documentViewController.clearSelectedText();
                break;
            case 9:
                this.currentToolHandler = new UnderLineAnnotationHandler(this.documentViewController, this);
                ((UnderLineAnnotationHandler)this.currentToolHandler).createTextMarkupAnnotation((ArrayList)null);
                this.documentViewController.clearSelectedText();
                break;
            case 11:
                this.currentToolHandler = new StrikeOutAnnotationHandler(this.documentViewController, this);
                ((StrikeOutAnnotationHandler)this.currentToolHandler).createTextMarkupAnnotation((ArrayList)null);
                this.documentViewController.clearSelectedText();
                break;
            case 12:
                this.currentToolHandler = new LineAnnotationHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
                break;
            case 13:
                this.currentToolHandler = new CustomLineArrowAnnotationHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
                break;
            case 14:
                this.currentToolHandler = new SquareAnnotationHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
                break;
            case 15:
                this.currentToolHandler = new CircleAnnotationHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
                break;
            case 16:
                this.currentToolHandler = new InkAnnotationHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
                break;
            case 17:
                this.currentToolHandler = new FreeTextAnnotationHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
                break;
            case 18:
                this.currentToolHandler = new TextAnnotationHandler(this.documentViewController, this);
                this.documentViewController.clearSelectedText();
        }

        if (this.currentToolHandler != null) {
            this.currentToolHandler.installTool();
            this.addMouseListener(this.currentToolHandler);
            this.addMouseMotionListener(this.currentToolHandler);
        }

    }
}
