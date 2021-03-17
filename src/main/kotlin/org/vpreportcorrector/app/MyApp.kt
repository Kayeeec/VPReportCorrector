package org.vpreportcorrector.app

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.stage.Stage
import org.vpreportcorrector.app.errorhandling.UncaughtErrorHandler
import org.vpreportcorrector.mainview.MainView
import org.vpreportcorrector.mainview.content.ContentViewModel
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.launch
import kotlin.system.exitProcess

class MyApp: App(MainView::class, Styles::class) {
    private val contentViewModel: ContentViewModel by inject()

    init {
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

    override fun start(stage: Stage) {
        super.start(stage)
        stage.onCloseRequest = EventHandler {
            if (!contentViewModel.checkUnsavedChanges()) it.consume()
        }
    }

    private fun onAppShutdown(){
        Platform.exit()
        exitProcess(0)
    }
}
