package org.vpreportcorrector.statistics.barchart2

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.vpreportcorrector.statistics.components.RefreshableInputComponent
import org.vpreportcorrector.statistics.components.RefreshableWizard
import org.vpreportcorrector.utils.p
import tornadofx.*

class BarChart2Wizard: Wizard(), RefreshableWizard {
    val barChart2Vm: BarChart2WizardViewModel by inject()

    init {
        title = "Bar chart 2"
        heading = "Comparison of issue counts in a given pair of weeks for a given team/seminar group"
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
        barChart2Vm.validate(decorateErrors = false)
    }

    override fun onBeforeShow() {
        resetWizard()
    }

    private fun resetWizard() {
        barChart2Vm.clear()
        refreshAllPages()
        isComplete = false
        currentPage = pages[0]
    }

    override fun refreshAllPages() {
        pages.forEach { (it as RefreshableInputComponent).refreshInputs() }
    }

    override fun onSave() {
        isComplete = barChart2Vm.commit()
    }
}


