package org.umlreviewer.statistics.charts.barchart3

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.collections.ObservableSet
import org.umlreviewer.enums.DiagramIssueGroup
import org.umlreviewer.utils.file.Team
import org.umlreviewer.utils.file.Week
import tornadofx.*

class BarChart3WizardParameters(
    initialDiagramTypes: ObservableSet<DiagramIssueGroup> = mutableSetOf<DiagramIssueGroup>().asObservable(),
    initialTeams: ObservableSet<Team> = mutableSetOf<Team>().asObservable(),
    initialWeek: Week? = null,
) {
    val diagramTypesProperty = SimpleSetProperty<DiagramIssueGroup>(initialDiagramTypes)
    var diagramTypes: ObservableSet<DiagramIssueGroup> by diagramTypesProperty

    val teamsProperty = SimpleSetProperty<Team>(initialTeams)
    var teams: ObservableSet<Team> by teamsProperty

    val weekProperty = SimpleObjectProperty<Week>(initialWeek)
    var week: Week? by weekProperty

    fun getTitle(): String {
        return "Bar chart 3: Issue counts by teams in a week ${week?.number} for given diagram types."
    }

    fun useTeamLongNames(): Boolean {
        return teams.map { it.seminarGroupFolder }.distinct().size > 1
    }
}
