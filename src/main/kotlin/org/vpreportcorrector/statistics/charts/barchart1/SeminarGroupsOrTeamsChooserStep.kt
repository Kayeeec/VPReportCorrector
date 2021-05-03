package org.vpreportcorrector.statistics.charts.barchart1

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.scene.Node
import org.vpreportcorrector.statistics.components.SeminarGroupsOrTeamsSelect
import org.vpreportcorrector.statistics.components.WizardPageView
import org.vpreportcorrector.statistics.enums.DataSelectMode
import tornadofx.*

class SeminarGroupsOrTeamsChooserStep : WizardPageView("Select team or seminar group") {
    val vm: BarChart1WizardViewModel by inject()
    val firstFieldNode: Node? by param<Node?>(null)
    override val errorMessage = stringBinding(this, vm.dataSelectMode) {
        if (vm.dataSelectMode.value == DataSelectMode.SEMINAR_GROUP)
            "No seminar groups selected."
        else
            "No teams selected."
    }
    override val isPageValid: BooleanBinding = vm.dataSelectMode.isEqualTo(DataSelectMode.SEMINAR_GROUP)
        .and(Bindings.size(vm.seminarGroups).greaterThan(0))
        .or(vm.dataSelectMode.isEqualTo(DataSelectMode.TEAM)
            .and(Bindings.size(vm.teams).greaterThan(0)))

    private val seminarGroupsOrTeamsSelectFragment = find<SeminarGroupsOrTeamsSelect>(mapOf(
        SeminarGroupsOrTeamsSelect::dataSelectMode to vm.dataSelectMode,
        SeminarGroupsOrTeamsSelect::teams to vm.teams,
        SeminarGroupsOrTeamsSelect::seminarGroups to vm.seminarGroups,
        SeminarGroupsOrTeamsSelect::firstFieldNode to firstFieldNode,
    ))

    init {
        addValidators()
    }

    override val root = borderpane {
        center = seminarGroupsOrTeamsSelectFragment.root
        bottom = errorLabelNode
    }

    override fun onSave() {
        isComplete = vm.commit(vm.dataSelectMode, vm.teams, vm.seminarGroups)
    }

    override fun refreshInputs() {
        seminarGroupsOrTeamsSelectFragment.refreshInputs()
    }

    override fun addValidators() {
        vm.addValidator(
            node = seminarGroupsOrTeamsSelectFragment.root,
            property = vm.dataSelectMode,
            validator = {
                if (vm.dataSelectMode.value == null)
                    error("No data select mode selected.")
                else
                    null
            }
        )
        vm.addValidator(
            node = seminarGroupsOrTeamsSelectFragment.root,
            property = vm.teams,
            validator = {
                if (vm.dataSelectMode.value == DataSelectMode.TEAM && vm.teams.isEmpty())
                    error("No teams selected.")
                else
                    null
            }
        )
        vm.addValidator(
            node = seminarGroupsOrTeamsSelectFragment.root,
            property = vm.seminarGroups,
            validator = {
                if (vm.dataSelectMode.value == DataSelectMode.SEMINAR_GROUP && vm.seminarGroups.isEmpty())
                    error("No seminar groups selected.")
                else
                    null
            }
        )
    }
}
