package org.umlreviewer.diagram

import javafx.application.Platform
import javafx.beans.property.SimpleSetProperty
import javafx.collections.ObservableSet
import org.icepdf.ri.common.ViewModel
import org.umlreviewer.enums.DiagramIssue
import org.umlreviewer.utils.AppConstants.DATA_FOLDER_NAME
import org.umlreviewer.utils.Helpers.getWorkingDirectory
import tornadofx.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger
import javax.json.JsonObject
import javax.json.JsonString
import javax.swing.SwingUtilities
import kotlin.error

class DiagramModel(val path: Path): JsonModel {
    private val log by lazy { Logger.getLogger(this.javaClass.name) }

    val swingController: CustomSwingController = CustomSwingController()
    val diagramIssuesProperty = SimpleSetProperty<DiagramIssue>(mutableSetOf<DiagramIssue>().asObservable())
    val diagramIssues: ObservableSet<DiagramIssue> by diagramIssuesProperty

    private var oldDiagramIssues: Set<DiagramIssue>? = null

    fun isModified(): Boolean {
        return diagramIssues != oldDiagramIssues
    }

    override fun updateModel(json: JsonObject) {
        with(json) {
            val issues = getJsonArray("diagramIssues").getValuesAs(JsonString::class.java)
                .map { DiagramIssue.valueOf(it.string) }
            diagramIssues.addAll(issues)
            if (oldDiagramIssues == null) {
                oldDiagramIssues = issues.toSet()
            }
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("relativePath", getRelativePathString())
            add("diagramIssues", diagramIssues.map { it.name })
        }
    }

    fun saveAsJsonToFile() {
        try {
            val savePath = getJsonFilePath()
            val parentDir = savePath.parent
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir)
            }
            save(savePath)
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            Platform.runLater {
                error(
                    title = "Error",
                    header = "Error occurred while saving diagram issues to JSON file.",
                    content = "Error message:\n${e.message}"
                )
            }
        }
    }

    fun savePdfDocument() {
        try {
            SwingUtilities.invokeAndWait {
                val file = path.toFile()
                val fileOutputStream = FileOutputStream(file)
                val buf = BufferedOutputStream(fileOutputStream, 8192)
                if (!swingController.document.stateManager.isChanged) {
                    swingController.document.writeToOutputStream(buf)
                } else {
                    swingController.document.saveToOutputStream(buf)
                }
                buf.flush()
                fileOutputStream.flush()
                buf.close()
                fileOutputStream.close()
                ViewModel.setDefaultFile(file)
            }
        } catch (e: Throwable) {
            log.severe(e.stackTraceToString())
            Platform.runLater {
                error(
                    title = "Error",
                    header = "Error occurred while saving PDF changes.",
                    content = "Error message:\n${e.message}"
                )
            }
        }
    }

    fun loadDiagramErrors() {
        val jsonPath = getJsonFilePath()
        if (Files.exists(jsonPath)) {
            val model = loadJsonObject(jsonPath)
            updateModel(model)
        }
    }

    private fun getRelativePathString(): String {
        val workdir = getWorkingDirectory()?.toFile() ?: error("Can't get relative path because working directory is null.")
        return path.toFile().toRelativeString(workdir)
    }

    private fun getJsonFilePath(): Path {
        val workdir = getWorkingDirectory()?.toFile() ?: error("Can't get relative path because working directory is null.")
        val relativeParent: File? = path.toFile().relativeTo(workdir).parentFile
        val jsonFileName = "${path.toFile().nameWithoutExtension}.json"
        return Paths.get(workdir.absolutePath, DATA_FOLDER_NAME, relativeParent?.path ?: "" , jsonFileName)
    }

}


