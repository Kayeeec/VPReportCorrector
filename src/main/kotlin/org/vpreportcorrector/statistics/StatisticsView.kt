package org.vpreportcorrector.statistics

import tornadofx.*

class StatisticsView : View("Statistics") {
    override val root = borderpane {
        center = vbox {
            text("Statistics")
        }
    }

    override fun onDock() {
        // TODO KB:
        log.info("statistics on dock")
    }

    override fun onUndock() {
        // TODO: 19.04.21
        log.info("statistics on undock")
    }
}
