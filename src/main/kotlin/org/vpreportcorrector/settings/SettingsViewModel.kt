package org.vpreportcorrector.settings

import org.vpreportcorrector.components.LoadingLatch
import org.vpreportcorrector.components.WithLoading
import tornadofx.ItemViewModel

abstract class SettingsViewModel<T: LoadableAndSavable>(item: T):
    ItemViewModel<T>(item), LoadableAndSavable, WithLoading by LoadingLatch()
