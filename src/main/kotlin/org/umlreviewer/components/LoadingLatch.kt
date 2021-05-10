package org.umlreviewer.components

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleIntegerProperty
import tornadofx.booleanBinding
import kotlin.math.max


interface WithLoading {
    val isLoading: BooleanBinding
    fun startLoading()
    fun endLoading()
}

open class LoadingLatch: WithLoading {
    private var loadingCountProperty = SimpleIntegerProperty(0)
    override val isLoading = booleanBinding(loadingCountProperty) {loadingCountProperty.value != 0 }
    override fun startLoading() {
        loadingCountProperty.value += 1
    }
    override fun endLoading() {
        loadingCountProperty.value = max(loadingCountProperty.value - 1, 0)
    }
}
