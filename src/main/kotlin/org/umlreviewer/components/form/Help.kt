package org.umlreviewer.components.form

import javafx.event.EventTarget
import javafx.scene.control.Label
import javafx.util.Duration
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.styles.Styles.Companion.helpIcon
import org.umlreviewer.styles.Styles.Companion.helpLabel
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.tooltip

fun EventTarget.help(tooltipText: String = "", op: Label.() -> Unit = {}) = Label("").attachTo(this, op) {
    it.apply {
        graphic = FontIcon(FontAwesomeRegular.QUESTION_CIRCLE).apply {
            addClass(helpIcon)
        }
        addClass(helpLabel)
        tooltip = tooltip {
            text = tooltipText
            showDelay = Duration(50.0)
            maxWidth = 350.0
            isWrapText = true
        }
    }
}
