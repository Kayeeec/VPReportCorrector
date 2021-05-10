package org.umlreviewer.settings

import org.umlreviewer.components.LoadingLatch
import org.umlreviewer.components.WithLoading
import tornadofx.ItemViewModel

abstract class SettingsViewModel<T: LoadableAndSavable>(item: T):
    ItemViewModel<T>(item), LoadableAndSavable, WithLoading by LoadingLatch()
