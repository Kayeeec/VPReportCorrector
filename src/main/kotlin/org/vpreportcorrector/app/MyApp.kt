package org.vpreportcorrector.app

import javafx.scene.Scene
import org.vpreportcorrector.mainview.MainView
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.launch

class MyApp: App(MainView::class, Styles::class) {
    fun main(args: Array<String>) {
        launch<MyApp>(args)
    }

    override fun createPrimaryScene(view: UIComponent) = Scene(view.root, 900.0, 700.0)

}
