package org.umlreviewer.diagram

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import org.icepdf.ri.common.PrintHelper
import org.icepdf.ri.common.SwingController
import org.icepdf.ri.common.ViewModel
import org.icepdf.ri.common.search.DocumentSearchControllerImpl
import org.umlreviewer.diagram.components.icepdf.CustomDocumentViewControllerImpl
import java.beans.PropertyChangeEvent
import java.util.*

class CustomSwingController: SwingController {
    private var reflectingZoomInZoomComboBox: Boolean = false
    val currentPageProperty = SimpleIntegerProperty(1)
    val nextAndLastPageButtonEnabled = SimpleBooleanProperty(true)
    val previousAndFirstPageButtonEnabled = SimpleBooleanProperty(false)

    constructor(currentMessageBundle: ResourceBundle? = null) {
        viewModel = ViewModel()
        documentViewController = CustomDocumentViewControllerImpl(this)
        documentSearchController = DocumentSearchControllerImpl(this)
        documentViewController.addPropertyChangeListener(this)
        if (currentMessageBundle != null) {
            SwingController.messageBundle = currentMessageBundle
        } else {
            SwingController.messageBundle = ResourceBundle.getBundle("org.icepdf.ri.resources.MessageBundle")
        }

        Thread { PrintHelper.preparePrintServices() }.start()
    }

    override fun propertyChange(evt: PropertyChangeEvent?) {
        super.propertyChange(evt)
        Platform.runLater {
            if (evt?.propertyName == "documentCurrentPage") {
                currentPageProperty.value = (evt.newValue as Int) + 1
            }
        }
    }

    fun goToNextPage() {
        if (currentPageNumber < document.numberOfPages - 1) {
            this.showPage(currentPageNumber + 1)
            recomputePageBtnsEnabled()
        }
    }

    fun goToPreviousPage() {
        if (currentPageNumber > 0) {
            this.showPage(currentPageNumber - 1)
            recomputePageBtnsEnabled()
        }
    }

    fun goToFirstPage() {
        if (currentPageNumber > 0) {
            this.showPage(0)
            recomputePageBtnsEnabled()
        }
    }

    fun goToLastPage() {
        if (currentPageNumber < document.numberOfPages) {
            this.showPage(document.numberOfPages - 1)
            recomputePageBtnsEnabled()
        }
    }

    private fun recomputePageBtnsEnabled() {
        Platform.runLater {
            nextAndLastPageButtonEnabled.value = currentPageNumber < document.numberOfPages - 1
            previousAndFirstPageButtonEnabled.value = currentPageNumber > 0
        }
    }
}
