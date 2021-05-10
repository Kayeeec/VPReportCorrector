package org.umlreviewer.utils

import javafx.event.EventTarget
import javafx.scene.text.TextFlow
import org.umlreviewer.app.Styles
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.text

/**
 * Typography helper functions. Must not be nested in TextFlow to preserve text wrapping.
 */

fun EventTarget.h1(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.h1)
    addClass(Styles.typographyText)
    text(text)
}

fun EventTarget.h2(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.h2)
    addClass(Styles.typographyText)
    text(text)
}

fun EventTarget.h3(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.h3)
    addClass(Styles.typographyText)
    text(text)
}

fun EventTarget.h4(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.h4)
    addClass(Styles.typographyText)
    text(text)
}


fun EventTarget.p(text: String, op: TextFlow.() -> Unit = {}) = TextFlow().attachTo(this, op).apply {
    addClass(Styles.p)
    addClass(Styles.typographyText)
    text(text){
        addClass("text")
    }
}

