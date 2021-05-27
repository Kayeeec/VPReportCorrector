package org.umlreviewer.diagram.components.icepdf;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.PageTree;
import org.icepdf.ri.common.views.AbstractDocumentViewModel;
import org.icepdf.ri.common.views.AbstractPageViewComponent;
import org.icepdf.ri.common.views.DocumentViewModel;

import java.util.ArrayList;

public class CustomDocumentViewModelImpl extends AbstractDocumentViewModel {
    public CustomDocumentViewModelImpl(Document document) {
        super(document);
        if (document != null) {
            PageTree pageTree = document.getPageTree();
            int numberOfPages = document.getNumberOfPages();
            int avgPageWidth = 0;
            int avgPageHeight = 0;
            this.pageComponents = new ArrayList(numberOfPages);

            for(int i = 0; i < numberOfPages; ++i) {
                AbstractPageViewComponent pageViewComponent;
                if (i < 10) {
                    pageViewComponent = this.buildPageViewComponent(this, pageTree, i, 0, 0);
                    avgPageWidth += pageViewComponent.getPreferredSize().width;
                    avgPageHeight += pageViewComponent.getPreferredSize().height;
                } else if (i > 10) {
                    pageViewComponent = this.buildPageViewComponent(this, pageTree, i, avgPageWidth, avgPageHeight);
                } else {
                    avgPageWidth /= 10;
                    avgPageHeight /= 10;
                    pageViewComponent = this.buildPageViewComponent(this, pageTree, i, avgPageWidth, avgPageHeight);
                }

                this.pageComponents.add(pageViewComponent);
            }
        }

    }

    protected AbstractPageViewComponent buildPageViewComponent(DocumentViewModel documentViewModel, PageTree pageTree, int pageIndex, int width, int height) {
        return new CustomPageViewComponentImpl(this, pageTree, pageIndex, width, height);
    }
}
