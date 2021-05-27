package org.umlreviewer.diagram.components.icepdf;

import org.icepdf.core.pobjects.Document;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;

public class CustomDocumentViewControllerImpl extends DocumentViewControllerImpl {

    public CustomDocumentViewControllerImpl(final SwingController viewerController) {
       super(viewerController);
    }

    public void setDocument(Document newDocument) {
        if (this.document != null) {
            this.document.dispose();
            this.document = null;
        }

        this.document = newDocument;
        if (this.documentViewModel != null) {
            this.documentViewModel.dispose();
            this.documentViewModel = null;
        }

        this.documentViewModel = new CustomDocumentViewModelImpl(this.document);
        if (this.document != null) {
            this.setViewType();
            this.documentViewScrollPane.addComponentListener(this);
            this.documentViewScrollPane.validate();
        }

    }
}
