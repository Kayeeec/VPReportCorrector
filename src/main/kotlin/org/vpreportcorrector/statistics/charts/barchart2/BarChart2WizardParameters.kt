package org.vpreportcorrector.statistics.charts.barchart2

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.collections.ObservableSet
import org.vpreportcorrector.enums.DiagramIssue
import org.vpreportcorrector.enums.DiagramIssueGroup
import org.vpreportcorrector.statistics.charts.ChartHelpers
import org.vpreportcorrector.statistics.enums.DataSelectMode
import org.vpreportcorrector.statistics.enums.IssueChooserMode
import org.vpreportcorrector.statistics.components.Team
import org.vpreportcorrector.statistics.components.Week
import tornadofx.*
import java.io.File

class BarChart2WizardParameters(
    issueMode: IssueChooserMode = IssueChooserMode.SINGLE_ISSUE,
    initialIssues: ObservableSet<DiagramIssue> = mutableSetOf<DiagramIssue>().asObservable(),
    initialIssueGroups: ObservableSet<DiagramIssueGroup> = mutableSetOf<DiagramIssueGroup>().asObservable(),
    initialDataSelectMode: DataSelectMode = DataSelectMode.TEAM,
    initialTeams: ObservableSet<Team> = mutableSetOf<Team>().asObservable(),
    initialSeminarGroups: ObservableSet<File> = mutableSetOf<File>().asObservable(),
    initialWeek1: Week? = null,
    initialWeek2: Week? = null
) {
    val issueSelectModeProperty = SimpleObjectProperty<IssueChooserMode>(issueMode)
    var issueSelectMode: IssueChooserMode by issueSelectModeProperty
    val issuesProperty = SimpleSetProperty<DiagramIssue>(initialIssues)
    var issues: ObservableSet<DiagramIssue> by issuesProperty
    val issueGroupsProperty = SimpleSetProperty<DiagramIssueGroup>(initialIssueGroups)
    var issueGroups: ObservableSet<DiagramIssueGroup> by issueGroupsProperty

    val dataSelectModeProperty = SimpleObjectProperty<DataSelectMode>(initialDataSelectMode)
    var dataSelectMode: DataSelectMode by dataSelectModeProperty
    val teamsProperty = SimpleSetProperty<Team>(initialTeams)
    var teams: ObservableSet<Team> by teamsProperty
    val seminarGroupsProperty = SimpleSetProperty<File>(initialSeminarGroups)
    var seminarGroups: ObservableSet<File> by seminarGroupsProperty

    val week1Property = SimpleObjectProperty<Week>(initialWeek1)
    var week1: Week? by week1Property
    val week2Property = SimpleObjectProperty<Week>(initialWeek2)
    var week2: Week? by week2Property

    fun getTitle(seminarGroup: File): String {
        return "${getTitleStart()} in weeks ${week1?.number} and ${week2?.number} for seminar group '${ChartHelpers.getSeminarGroupName(seminarGroup)}'"
    }

    fun getTitle(team: Team): String {
        return "${getTitleStart()} in weeks ${week1?.number} and ${week2?.number} for team '${team.longName}'"
    }

    private fun getTitleStart() =
        if (issueSelectMode == IssueChooserMode.ISSUE_GROUP) "Bar chart 2: Compare issue counts by issue group"
        else "Bar chart 2: Compare issue counts"

}
