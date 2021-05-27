package org.umlreviewer.diagram.components.icepdf;

import org.icepdf.core.pobjects.annotations.LineAnnotation;
import org.icepdf.ri.common.tools.LineArrowAnnotationHandler;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewController;

public class CustomLineArrowAnnotationHandler extends LineArrowAnnotationHandler {

    public CustomLineArrowAnnotationHandler(DocumentViewController documentViewController, AbstractPageViewComponent pageViewComponent) {
        super(documentViewController, pageViewComponent);
        startLineEnding = LineAnnotation.LINE_END_NONE;
        endLineEnding = LineAnnotation.LINE_END_OPEN_ARROW;
    }
}
