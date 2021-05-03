package org.vpreportcorrector.statistics.charts.barchart1

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.statistics.components.RefreshableInputComponent
import org.vpreportcorrector.statistics.components.RefreshableWizard
import org.vpreportcorrector.utils.p
import tornadofx.*

class BarChart1Wizard: Wizard(), RefreshableWizard {
    val barChart1Vm: BarChart1WizardViewModel by inject()

    init {
        title = "Bar chart 1"
        heading = "Issue counts in a given week for a given team/seminar group"
        graphic = label {
            graphic = FontIcon(FontAwesomeSolid.CHART_BAR)
            style  {
                fontSize = 60.px
            }
        }
        add(find<IssueChooserStep>())

        add(find<SeminarGroupsOrTeamsChooserStep>(mapOf(
            SeminarGroupsOrTeamsChooserStep::firstFieldNode to p("Selecting n items will result in generating n charts."),
        )))

        add(find<WeekChooserStep>())
        barChart1Vm.validate(decorateErrors = false)
    }

    override fun onBeforeShow() {
        resetWizard()
    }

    private fun resetWizard() {
        barChart1Vm.clear()
        refreshAllPages()
        isComplete = false
        currentPage = pages[0]
    }

    override fun refreshAllPages() {
        pages.forEach { (it as RefreshableInputComponent).refreshInputs() }
    }

    override fun onSave() {
        isComplete = barChart1Vm.commit()
    }
}


