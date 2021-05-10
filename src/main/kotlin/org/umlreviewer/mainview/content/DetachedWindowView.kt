package org.umlreviewer.mainview.content

import tornadofx.*

class DetachedWindowView : View("Detached window") {
    val tabPane = tabpane {
        id = TabLocation.DETACHED_WINDOW.name
    }
    private val cvm: ContentViewModel by inject(FX.defaultScope)

    init {
        tabPane.tabs.onChange {
            if (tabPane.tabs.isEmpty()) close()
        }
    }

    override fun onBeforeShow() {
        modalStage?.let {
            it.minWidth = 900.0
            it.minHeight = 800.0
        }
        modalStage?.setOnCloseRequest {
            if (!cvm.disposeDetachedTabsAndClose()) {
                it.consume()
            }
        }
    }

    override val root = borderpane {
        center = tabPane
        tabPane.fitToParentSize()
    }
}
