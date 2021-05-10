package org.umlreviewer.statistics

import org.umlreviewer.enums.DiagramIssue
import org.umlreviewer.utils.file.FileTreeHelpers
import tornadofx.JsonBuilder
import tornadofx.JsonModel
import tornadofx.loadJsonObject
import java.nio.file.Files
import java.nio.file.Path
import javax.json.JsonObject
import javax.json.JsonString

class PdfJsonData(): JsonModel {
    val diagramIssues = mutableSetOf<DiagramIssue>()

    override fun updateModel(json: JsonObject) {
        with(json) {
            val issues = getJsonArray("diagramIssues").getValuesAs(JsonString::class.java)
                .map { DiagramIssue.valueOf(it.string) }
            diagramIssues.addAll(issues)
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("diagramIssues", diagramIssues.map { it.name })
        }
    }

    fun load(path: Path) {
        val jsonPath = FileTreeHelpers.getJsonFilePath(path)
        if (Files.exists(jsonPath)) {
            val model = loadJsonObject(jsonPath)
            updateModel(model)
        }
    }
}
