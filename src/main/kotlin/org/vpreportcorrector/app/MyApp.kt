package org.vpreportcorrector.app

import javafx.scene.Scene
import org.vpreportcorrector.mainview.MainView
import tornadofx.*

class MyApp: App(MainView::class, Styles::class) {
    init {
        // enables "hot reload" in debug mode
//        reloadStylesheetsOnFocus()
//        reloadViewsOnFocus()
    }
    fun main(args: Array<String>) {
        launch<MyApp>(args)
    }

    override fun createPrimaryScene(view: UIComponent) = Scene(view.root, 900.0, 700.0)

}
