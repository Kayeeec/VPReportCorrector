package org.vpreportcorrector.statistics.components

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.scene.Node
import org.vpreportcorrector.statistics.enums.DataSelectMode
import tornadofx.*
import java.io.File

class SeminarGroupsOrTeamsSelect : Fragment("Select team or seminar group"), RefreshableInputComponent {
    val dataSelectMode: SimpleObjectProperty<DataSelectMode> by param()
    val teams: SimpleSetProperty<Team> by param()
    val seminarGroups: SimpleSetProperty<File> by param()
    val firstFieldNode: Node? by param<Node?>(null)

    private var seminarGroupsSelectFragment = find<SeminarGroupMultiSelect>(mapOf(
        SeminarGroupMultiSelect::selectedSeminarGroups to seminarGroups
    ))
    private var teamsSelectFragment = find<TeamMultiSelect>(mapOf(TeamMultiSelect::selectedTeams to teams))

    override val root = form {
        fieldset(title) {
            if (firstFieldNode != null) {
                field {
                    add(firstFieldNode!!)
                }
            }
            togglegroup {
                bind(dataSelectMode)
                field {
                    radiobutton("Select teams", group = this@togglegroup, value = DataSelectMode.TEAM) {
                        isSelected = dataSelectMode.value == DataSelectMode.TEAM
                    }
                }
                field {
                    radiobutton(
                        "Select seminar groups",
                        group = this@togglegroup,
                        value = DataSelectMode.SEMINAR_GROUP
                    ) {
                        isSelected = dataSelectMode.value == DataSelectMode.SEMINAR_GROUP
                    }
                }
            }
            field {
                vbox {
                    dynamicContent(dataSelectMode) {
                        if (dataSelectMode.value == DataSelectMode.SEMINAR_GROUP) {
                            add(seminarGroupsSelectFragment)
                        } else {
                            add(teamsSelectFragment)
                        }
                    }
                }
            }
        }
    }

    override fun refreshInputs() {
        seminarGroupsSelectFragment.refreshInputs()
        teamsSelectFragment.refreshInputs()
    }
}

