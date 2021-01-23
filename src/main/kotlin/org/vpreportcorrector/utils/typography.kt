package org.vpreportcorrector.utils

import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.vpreportcorrector.app.Styles
import tornadofx.*

fun H1(text: String = "", bindWidth: Boolean = false): HBox {
    return styledTextNode(Styles.h1, text, bindWidth)
}

fun H2(text: String = "", bindWidth: Boolean = false): HBox {
    return styledTextNode(Styles.h2, text, bindWidth)
}

fun H3(text: String = "", bindWidth: Boolean = false): HBox {
    return styledTextNode(Styles.h2, text, bindWidth)
}

fun P(text: String, bindWidth: Boolean = false): HBox {
    return styledTextNode(Styles.p, text, bindWidth)
}

private fun styledTextNode(cssClass: CssRule, text: String, bindWidth: Boolean = false): HBox {
    return HBox().apply {
        hgrow = Priority.ALWAYS
        val widthProp = widthProperty()
        addClass(cssClass)
        text(text) {
            if (bindWidth) {
                wrappingWidthProperty().bind(widthProp.subtract(1.0))
            }
        }
    }
}

