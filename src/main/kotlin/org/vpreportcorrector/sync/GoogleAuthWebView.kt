package org.vpreportcorrector.sync

import tornadofx.*

class GoogleAuthWebView : View("Google authentication") {
    val url: String by param()

    private val wv = webview()
    private val engine = wv.engine

    override fun onBeforeShow() {
        engine.load(url)
    }

    override val root = wv
}
