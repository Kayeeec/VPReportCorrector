package org.umlreviewer.statistics.charts.barchart1

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.collections.ObservableSet
import org.umlreviewer.enums.DiagramIssue
import org.umlreviewer.enums.DiagramIssueGroup
import org.umlreviewer.statistics.charts.ChartHelpers
import org.umlreviewer.statistics.enums.DataSelectMode
import org.umlreviewer.statistics.enums.IssueChooserMode
import org.umlreviewer.utils.file.Team
import org.umlreviewer.utils.file.Week
import tornadofx.*
import java.io.File

class BarChart1WizardParameters(
    issueMode: IssueChooserMode = IssueChooserMode.SINGLE_ISSUE,
    initialIssues: ObservableSet<DiagramIssue> = mutableSetOf<DiagramIssue>().asObservable(),
    initialIssueGroups: ObservableSet<DiagramIssueGroup> = mutableSetOf<DiagramIssueGroup>().asObservable(),
    initialDataSelectMode: DataSelectMode = DataSelectMode.TEAM,
    initialTeams: ObservableSet<Team> = mutableSetOf<Team>().asObservable(),
    initialSeminarGroups: ObservableSet<File> = mutableSetOf<File>().asObservable(),
    initialWeek: Week? = null
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

    val weekProperty = SimpleObjectProperty<Week>(initialWeek)
    var week: Week? by weekProperty

    fun getTitle(seminarGroup: File): String {
        return "${getTitleStart()} in week ${week?.number} for seminar group '${ChartHelpers.getSeminarGroupName(seminarGroup)}'"
    }

    fun getTitle(team: Team): String {
        return "${getTitleStart()} in week ${week?.number} for team '${team.longName}'"
    }

    private fun getTitleStart() =
        if (issueSelectMode == IssueChooserMode.ISSUE_GROUP) "Bar chart 1: Issue counts by issue group"
        else "Bar chart 1: Issue counts"
}
