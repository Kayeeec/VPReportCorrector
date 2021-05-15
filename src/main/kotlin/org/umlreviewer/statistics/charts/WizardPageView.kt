package org.umlreviewer.statistics.charts

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.scene.Node
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.statistics.components.RefreshableInputComponent
import org.umlreviewer.styles.Styles
import tornadofx.*

abstract class WizardPageView(title: String? = null, icon: Node? = null): View(title, icon), RefreshableInputComponent {
    /**
     * Manually created boolean binding to tell if the page is valid and the 'Next' button is enabled.
     * Workaround for partial validation and commits not working properly.
     */
    abstract val isPageValid: BooleanBinding
    override val complete by lazy { isPageValid }

    /**
     * Error message to be shown on of the page if the page is not valid.
     */
    abstract val errorMessage: StringBinding

    /**
     * Label node to for showing the error message. Embed manually at a convenient place.
     */
    protected val errorLabelNode by lazy {
        vbox {
            label {
                addClass(Styles.textError)
                textProperty().bind(errorMessage)
                graphic = FontIcon(FontAwesomeSolid.EXCLAMATION_CIRCLE).apply { addClass(Styles.textError) }
                hiddenWhen { isPageValid }
            }
        }
    }

    /**
     * Adds validators to fields/view model properties tied to this page.
     * Should be run in the init block of the implementing class.
     */
    protected abstract fun addValidators()

}
