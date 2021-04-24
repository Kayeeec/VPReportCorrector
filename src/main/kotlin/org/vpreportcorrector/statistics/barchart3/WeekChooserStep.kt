package org.vpreportcorrector.statistics.barchart3

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import org.vpreportcorrector.statistics.components.WeekSingleSelect
import org.vpreportcorrector.statistics.components.WizardPageView
import tornadofx.*

class WeekChooserStep : WizardPageView("Select week") {
    val vm: BarChart3WizardViewModel by inject()

    override val isPageValid: BooleanBinding = vm.week.isNotNull
    override val errorMessage: StringBinding = stringBinding(this, vm.week) {
        if (vm.week.value == null)
            "No week selected."
        else
            ""
    }
    private var weekSingleSelectFragment = find<WeekSingleSelect>(mapOf(
        WeekSingleSelect::selectedWeek to vm.week,
    ))

    init {
        addValidators()
    }

    override val root = borderpane {
        center = form {
            fieldset(title) {
                field {
                    add(weekSingleSelectFragment)
                }
            }
        }
        bottom = errorLabelNode
    }

    override fun onSave() {
        isComplete = vm.commit(vm.week)
    }

    override fun refreshInputs() {
        weekSingleSelectFragment.refreshInputs()
    }

    override fun addValidators() {
        vm.addValidator(
            node = weekSingleSelectFragment.root,
            property = vm.week,
            trigger = ValidationTrigger.OnChange(),
            validator = {
                if (vm.week.value == null)
                    error("No week selected.")
                else
                    null
            }
        )
    }
}
