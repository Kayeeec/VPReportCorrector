package org.vpreportcorrector.app

import tornadofx.FXEvent
import java.nio.file.Path

object SettingsChanged : FXEvent()
object RefreshFilesExplorer : FXEvent()

class OpenDiagramEvent(val path: Path): FXEvent()
class OpenDiagramInNewWindowEvent(val path: Path): FXEvent()

class DiagramSavedEvent(val path: Path): FXEvent()
class DiagramInEditModeEvent(val path: Path): FXEvent()

class ResizeEditorEvent(val width: Double, val height: Double): FXEvent()
