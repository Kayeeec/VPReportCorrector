package org.umlreviewer.statistics.charts.barchart1

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import org.umlreviewer.styles.Styles.Companion.textError
import org.umlreviewer.statistics.components.IssueChooserFragment
import org.umlreviewer.statistics.components.WizardPageView
import org.umlreviewer.statistics.enums.IssueChooserMode
import tornadofx.*

class IssueChooserStep : WizardPageView("Select issues or issue groups") {
    val vm: BarChart1WizardViewModel by inject()
    override val errorMessage = stringBinding(this, vm.issueSelectMode) {
        if (vm.issueSelectMode.value == IssueChooserMode.SINGLE_ISSUE)
            "No issues selected."
        else
            "No issue groups selected."
    }

    // workaround - some bug prevents validators to work correctly on partial commits and validations
    override val isPageValid: BooleanBinding = vm.issueSelectMode.isEqualTo(IssueChooserMode.SINGLE_ISSUE)
            .and(Bindings.size(vm.issues).greaterThan(0))
        .or(
            vm.issueSelectMode.isEqualTo(IssueChooserMode.ISSUE_GROUP)
                .and(Bindings.size(vm.issueGroups).greaterThan(0))
        )

    private val issueChooserFragment = find<IssueChooserFragment>(
        mapOf(
            IssueChooserFragment::issueSelectMode to vm.issueSelectMode,
            IssueChooserFragment::issueGroups to vm.issueGroups,
            IssueChooserFragment::issues to vm.issues,
        )
    )

    init {
        addValidators()
    }

    override val root = borderpane {
        center = issueChooserFragment.root
        bottom = vbox {
            label {
                addClass(textError)
                textProperty().bind(errorMessage)
                graphic = FontIcon(FontAwesomeSolid.EXCLAMATION_CIRCLE).apply { addClass(textError) }
                hiddenWhen { isPageValid }
            }
        }
    }

    override fun onSave() {
        isComplete = vm.commit(vm.issueSelectMode, vm.issueGroups, vm.issues)
    }

    override fun refreshInputs() {
        issueChooserFragment.refreshInputs()
    }

    override fun addValidators() {
        vm.addValidator(
            node = issueChooserFragment.root,
            property = vm.issueGroups,
            validator = {
                if (vm.issueSelectMode.value == IssueChooserMode.ISSUE_GROUP && vm.issueGroups.value.isEmpty())
                    error("No issue groups selected.")
                else
                    null
            }
        )
        vm.addValidator(
            node = issueChooserFragment.root,
            property = vm.issues,
            trigger = ValidationTrigger.OnChange(),
            validator = {
                if (vm.issueSelectMode.value == IssueChooserMode.SINGLE_ISSUE && vm.issues.value.isEmpty())
                    error("No issues selected.")
                else
                    null
            }
        )
        vm.addValidator(
            node = issueChooserFragment.root,
            property = vm.issueSelectMode,
            trigger = ValidationTrigger.OnChange(),
            validator = {
                if (vm.issueSelectMode.value == null)
                    error("No issue selection mode selected.")
                else
                    null
            }
        )
    }
}
