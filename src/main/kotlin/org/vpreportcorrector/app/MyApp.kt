package org.vpreportcorrector.app

import org.vpreportcorrector.view.MainView
import tornadofx.App
import tornadofx.launch

class MyApp: App(MainView::class, Styles::class) {
    fun main(args: Array<String>) {
        launch<MyApp>(args)
    }
}
