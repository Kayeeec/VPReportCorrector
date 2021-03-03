package utils

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import types.SearchedPage
import types.TextPositionSequence
import java.io.IOException

/**
 * @param page 1-based page number
 */
fun searchPageForDiagramHeadings(document: PDDocument, page: Int): SearchedPage {
    var lineNumber = 0
    val searchedPage = SearchedPage()
    val searchTerms = listOf(START_HEADER, END_HEADER)

    val stripper: PDFTextStripper = object : PDFTextStripper() {
        @Throws(IOException::class)
        override fun writeString(text: String, textPositions: List<TextPosition>) {
            val word = TextPositionSequence(textPositions)
            val string = word.toString()
            searchedPage.addLine(lineNumber, word)

            for (searchTerm in searchTerms) {
                var fromIndex = 0
                var index = string.indexOf(searchTerm, fromIndex)
                while (index > -1) {
                    searchedPage.addLineHit(searchTerm, lineNumber, word.fontSize)
                    fromIndex = index + 1
                    index = string.indexOf(searchTerm, fromIndex)
                }
            }
            super.writeString(text, textPositions)
        }

        override fun writeLineSeparator() {
            lineNumber += 1
            super.writeLineSeparator()
        }
    }

    stripper.sortByPosition = true
    stripper.startPage = page
    stripper.endPage = page
    stripper.getText(document)
    return searchedPage
}


























