package org.vpreportcorrector.diagramextractor.types

import org.apache.pdfbox.pdmodel.PDPage
import org.vpreportcorrector.diagramextractor.types.SearchedPage

data class DiagramPageResult(
    val pageNumber: Int, // 1-based
    val pdPage: PDPage,
    val searchedPage: SearchedPage,
)
