package org.vpreportcorrector.components

import javafx.beans.property.SimpleIntegerProperty
import tornadofx.booleanBinding
import kotlin.math.max

open class LoadingLatch {
    private var loadingCountProperty = SimpleIntegerProperty(0)
    val isLoading = booleanBinding(loadingCountProperty) {loadingCountProperty.value != 0 }
    fun startLoading() {
        loadingCountProperty.value += 1
    }
    fun endLoading() {
        loadingCountProperty.value = max(loadingCountProperty.value - 1, 0)
    }
}
