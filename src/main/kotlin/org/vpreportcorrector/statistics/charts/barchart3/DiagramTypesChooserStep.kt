package org.vpreportcorrector.statistics.charts.barchart3

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import org.vpreportcorrector.statistics.components.DiagramIssueGroupMultiselect
import org.vpreportcorrector.statistics.components.WizardPageView
import tornadofx.*

class DiagramTypesChooserStep : WizardPageView("Select diagram types") {
    val vm: BarChart3WizardViewModel by inject()
    override val isPageValid: BooleanBinding = Bindings.size(vm.diagramTypes).greaterThan(0)
    override val errorMessage: StringBinding = stringBinding(this, vm.diagramTypes) {
        if (vm.diagramTypes.isEmpty())
            "No diagram types selected."
        else
            ""
    }

    private val diagramTypeMultiSelectFragment = find<DiagramIssueGroupMultiselect>(mapOf(
        DiagramIssueGroupMultiselect::diagramIssueGroups to vm.diagramTypes,
        DiagramIssueGroupMultiselect::treeRootLabel to "Diagram types"
    ))

    init {
        addValidators()
    }

    override val root = borderpane {
        center = form {
            fieldset(title) {
                field {
                    add(diagramTypeMultiSelectFragment)
                }
            }
        }
        bottom = errorLabelNode
    }

    override fun onSave() {
        isComplete = vm.commit(vm.diagramTypes)
    }

    override fun refreshInputs() {
        diagramTypeMultiSelectFragment.refreshInputs()
    }

    override fun addValidators() {
        vm.addValidator(
            node = diagramTypeMultiSelectFragment.root,
            property = vm.diagramTypes,
            trigger = ValidationTrigger.OnChange(),
            validator = {
                if (vm.diagramTypes.value.isEmpty())
                    error("No diagram types selected.")
                else
                    null
            }
        )
    }
}
