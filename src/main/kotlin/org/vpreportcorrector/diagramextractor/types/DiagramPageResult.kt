package types

import org.apache.pdfbox.pdmodel.PDPage

data class DiagramPageResult(
    val pageNumber: Int, // 1-based
    val pdPage: PDPage,
    val searchedPage: SearchedPage,
)
