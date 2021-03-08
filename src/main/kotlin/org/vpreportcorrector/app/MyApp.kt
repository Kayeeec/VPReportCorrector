package org.vpreportcorrector.app

import javafx.application.Platform
import javafx.scene.Scene
import org.vpreportcorrector.app.errorhandling.UncaughtErrorHandler
import org.vpreportcorrector.mainview.MainView
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.launch
import kotlin.system.exitProcess

class MyApp: App(MainView::class, Styles::class) {
    init {
        // enables "hot reload" in debug mode
//        reloadStylesheetsOnFocus()
//        reloadViewsOnFocus()
        Thread.setDefaultUncaughtExceptionHandler(UncaughtErrorHandler())
    }
    fun main(args: Array<String>) {
        launch<MyApp>(args)
    }

    override fun createPrimaryScene(view: UIComponent) = Scene(view.root, 1500.0, 1000.0)

    override fun stop() {
        super.stop()
        onAppShutdown()
    }

    private fun onAppShutdown(){
        Platform.exit()
        exitProcess(0)
    }

}
