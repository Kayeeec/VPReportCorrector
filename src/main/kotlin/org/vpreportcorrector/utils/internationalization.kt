package org.vpreportcorrector.utils

import tornadofx.Component
import tornadofx.get
import java.text.MessageFormat

/**
 * Extension function for easier internationalization. Created to easily support parameters.
 * @param key   message key
 * @param args  arguments to fill the message pattern
 */
fun Component.t(key: String, vararg args: Any?): String {
    return MessageFormat.format(this.messages[key], *args.filterNotNull().toTypedArray())
}
