package org.umlreviewer.app

import tornadofx.FXEvent
import java.nio.file.Path

object SettingsChanged : FXEvent()
object RefreshFilesExplorer : FXEvent()

class OpenDiagramEvent(val path: Path): FXEvent()
class OpenDiagramInNewWindowEvent(val path: Path): FXEvent()

class DiagramSavedEvent(val path: Path): FXEvent()
class DiagramToggleEditModeEvent(val path: Path, val isEditing: Boolean): FXEvent()

class ResizeEditorEvent(val width: Double, val height: Double): FXEvent()

object RequestSync : FXEvent()
