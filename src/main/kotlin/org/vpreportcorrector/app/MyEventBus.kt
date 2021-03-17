package org.vpreportcorrector.app

import tornadofx.FXEvent
import java.nio.file.Path

object SettingsChanged : FXEvent()
object RefreshFilesExplorer : FXEvent()

class EditDiagramEvent(val path: Path): FXEvent()
class ViewDiagramEvent(val path: Path): FXEvent()

class DiagramSavedEvent(val path: Path): FXEvent()

