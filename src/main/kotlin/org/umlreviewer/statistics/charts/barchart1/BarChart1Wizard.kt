package org.umlreviewer.statistics.charts.barchart1

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.statistics.components.RefreshableInputComponent
import org.umlreviewer.statistics.charts.RefreshableWizard
import org.umlreviewer.utils.p
import tornadofx.*

class BarChart1Wizard: RefreshableWizard() {
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

    override fun onSave() {
        isComplete = barChart1Vm.commit()
    }
}


