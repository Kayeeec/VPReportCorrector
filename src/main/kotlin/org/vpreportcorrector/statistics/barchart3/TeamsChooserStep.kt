package org.vpreportcorrector.statistics.barchart3

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import org.vpreportcorrector.statistics.components.TeamMultiSelect
import org.vpreportcorrector.statistics.components.WizardPageView
import tornadofx.*

class TeamsChooserStep : WizardPageView("Select teams") {
    val vm: BarChart3WizardViewModel by inject()
    override val isPageValid: BooleanBinding = Bindings.size(vm.teams).greaterThan(0)
    override val errorMessage: StringBinding = stringBinding(this, vm.teams) {
        if (vm.teams.isEmpty())
            "No teams selected."
        else
            ""
    }

    private val teamsSelectFragment = find<TeamMultiSelect>(mapOf(
        TeamMultiSelect::selectedTeams to vm.teams
    ))

    init {
        addValidators()
    }

    override val root = borderpane {
        center = form {
            fieldset(title) {
                field {
                    add(teamsSelectFragment)
                }
            }
        }
        bottom = errorLabelNode
    }

    override fun onSave() {
        isComplete = vm.commit(vm.teams)
    }

    override fun refreshInputs() {
        teamsSelectFragment.refreshInputs()
    }

    override fun addValidators() {
        vm.addValidator(
            node = teamsSelectFragment.root,
            property = vm.teams,
            trigger = ValidationTrigger.OnChange(),
            validator = {
                if (vm.teams.value.isEmpty())
                    error("No teams selected.")
                else
                    null
            }
        )
    }
}
