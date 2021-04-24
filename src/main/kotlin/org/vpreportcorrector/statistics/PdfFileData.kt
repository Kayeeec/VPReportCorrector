package org.vpreportcorrector.statistics

import org.vpreportcorrector.enums.DiagramIssue
import org.vpreportcorrector.enums.DiagramIssueGroup
import org.vpreportcorrector.utils.FileTreeHelpers
import org.vpreportcorrector.utils.Helpers
import java.io.File

data class PdfFileData(
    val pdfFile: File,
    private val seminarGroup: File
) {
    val relativePath: String
    val week: Int?
    val team: Int?
    val issues = mutableSetOf<DiagramIssue>()
    val issueGroups = mutableSetOf<DiagramIssueGroup>()

    init {
        val workdir = Helpers.getWorkingDirectory()?.toFile()
        relativePath = if (workdir != null) pdfFile.relativeTo(workdir).toString() else pdfFile.path
        week = FileTreeHelpers.getWeek(relativePath)
        team = FileTreeHelpers.getTeam(relativePath)
        val jsonData = PdfJsonData()
        jsonData.load(pdfFile.toPath())
        issues.addAll(jsonData.diagramIssues)
        issueGroups.addAll(issues.map { DiagramIssueGroup.getGroup(it) }.filterNotNull())
    }

    fun getIssueCountsPerGroup(): Map<DiagramIssueGroup, Int> {
        val result = mutableMapOf<DiagramIssueGroup, Int>()
        issueGroups.forEach { grp -> result[grp] = 0 }
        issues.forEach { di ->
            val key = DiagramIssueGroup.getGroup(di)!!
            result[key] = result[key]?.plus(1) ?: 1
        }
        return result.toMap()
    }
}
