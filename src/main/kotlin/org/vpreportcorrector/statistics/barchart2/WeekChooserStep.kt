package org.vpreportcorrector.statistics.barchart2

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.scene.layout.Priority
import org.vpreportcorrector.statistics.components.WeekSingleSelect
import org.vpreportcorrector.statistics.components.WizardPageView
import tornadofx.*

class WeekChooserStep : WizardPageView("Select weeks") {
    val vm: BarChart2WizardViewModel by inject()
    override val isPageValid: BooleanBinding = vm.week1.isNotNull.and(vm.week2.isNotNull)
    override val errorMessage: StringBinding = stringBinding(this, vm.week1, vm.week2) {
        if (vm.week1.value == null && vm.week2.value == null)
            "No weeks selected."
        else if (vm.week1.value == null)
            "First week not selected."
        else if (vm.week2.value == null)
            "Second week not selected."
        else if (weeksHaveEqualNumbers())
            "First and second week have the same number."
        else
            ""
    }
    private var week1SingleSelectFragment = find<WeekSingleSelect>(mapOf(
        WeekSingleSelect::selectedWeek to vm.week1,
        WeekSingleSelect::labelText to "First week:"
    ))
    private var week2SingleSelectFragment = find<WeekSingleSelect>(mapOf(
        WeekSingleSelect::selectedWeek to vm.week2,
        WeekSingleSelect::labelText to "Second week:"
    ))

    init {
        addValidators()
    }

    override val root = borderpane {
        center = form {
            fieldset(title) {
                gridpane {
                    row {
                        vbox {
                            gridpaneColumnConstraints { percentWidth = 50.0 }
                            gridpaneConstraints { marginRight = 5.0 }
                            add(week1SingleSelectFragment)
                        }
                        vbox {
                            gridpaneColumnConstraints { percentWidth = 50.0 }
                            gridpaneConstraints { marginLeft = 5.0 }
                            add(week2SingleSelectFragment)
                        }
                    }
                }
            }
        }
        bottom = errorLabelNode
    }

    override fun onSave() {
        isComplete = vm.commit(vm.week1, vm.week2)
    }

    override fun refreshInputs() {
        week1SingleSelectFragment.refreshInputs()
        week2SingleSelectFragment.refreshInputs()
    }

    override fun addValidators() {
        vm.addValidator(
            node = week1SingleSelectFragment.root,
            property = vm.week1,
            trigger = ValidationTrigger.OnChange(),
            validator = {
                if (vm.week1.value == null)
                    error("First week not selected.")
                else if (weeksHaveEqualNumbers())
                    error("First and second week have the same number.")
                else
                    null
            }
        )
        vm.addValidator(
            node = week2SingleSelectFragment.root,
            property = vm.week2,
            trigger = ValidationTrigger.OnChange(),
            validator = {
                if (vm.week2.value == null)
                    error("Second week not selected.")
                else if (weeksHaveEqualNumbers())
                    error("First and second week have the same number.")
                else
                    null
            }
        )
    }

    private fun weeksHaveEqualNumbers() =
        vm.week1.value != null && vm.week2.value != null && vm.week1.value.number == vm.week2.value.number
}
