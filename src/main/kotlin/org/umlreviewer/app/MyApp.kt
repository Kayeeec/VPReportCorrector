package org.umlreviewer.app

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.stage.Stage
import org.umlreviewer.app.errorhandling.UncaughtErrorHandler
import org.umlreviewer.mainview.MainView
import org.umlreviewer.mainview.content.ContentViewModel
import org.umlreviewer.sync.SyncController
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.launch
import kotlin.system.exitProcess

class MyApp: App(MainView::class, Styles::class) {
    private val contentViewModel: ContentViewModel by inject()
    private val syncController: SyncController by inject()

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
            if (!contentViewModel.checkUnsavedChanges()
                || syncController.isAnyTaskRunning.value) it.consume()
        }
    }

    private fun onAppShutdown(){
        Platform.exit()
        exitProcess(0)
    }
}
