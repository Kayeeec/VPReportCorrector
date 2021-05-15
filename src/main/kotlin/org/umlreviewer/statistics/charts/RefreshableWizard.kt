package org.umlreviewer.statistics.charts

import org.umlreviewer.statistics.components.RefreshableInputComponent
import tornadofx.*

abstract class RefreshableWizard(): Wizard() {
    fun refreshAllPages() {
        pages.forEach { (it as RefreshableInputComponent).refreshInputs() }
    }
}
