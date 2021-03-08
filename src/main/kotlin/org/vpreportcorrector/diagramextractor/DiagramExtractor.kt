import org.apache.batik.anim.dom.SVGDOMImplementation
import org.apache.batik.svggen.SVGGeneratorContext
import org.apache.batik.svggen.SVGGraphics2D
import org.apache.pdfbox.contentstream.operator.Operator
import org.apache.pdfbox.cos.COSInteger
import org.apache.pdfbox.pdfparser.PDFStreamParser
import org.apache.pdfbox.pdfwriter.ContentStreamWriter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.common.PDStream
import org.apache.pdfbox.rendering.PDFRenderer
import org.vpreportcorrector.diagramextractor.exceptions.DiagramExtractorException
import org.vpreportcorrector.utils.FileConflictChoice
import org.vpreportcorrector.utils.findConflictingFile
import org.vpreportcorrector.utils.openFileExistsDialog
import org.vpreportcorrector.utils.suggestName
import org.w3c.dom.Element
import types.DiagramPageResult
import utils.searchPageForDiagramHeadings
import java.awt.Color
import java.awt.Dimension
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.floor
import kotlin.math.max

class DiagramExtractor(
    inputPdf: File,
    private val outputDir: File,
    var rememberChoicePdf: FileConflictChoice?
) {
    private val diagramPages = mutableListOf<DiagramPageResult>()
    private var maxDiagramPageNumber = 0
    private var fileName: String = inputPdf.nameWithoutExtension
    private var document: PDDocument = PDDocument.load(inputPdf)
    private var template: PDDocument = PDDocument.load(inputPdf)
    private val paddingLength: Int
        get() = "${this.maxDiagramPageNumber}".length

    fun extractDiagrams() {
        this.document.use { pdDocument: PDDocument ->
            this.template.use { pdTemplate: PDDocument ->
                this.clearTemplatePages()
                this.collectDiagramPages(pdDocument)
                for (dpr: DiagramPageResult in this.diagramPages) {
                    this.importPageIntoTemplate(dpr.pageNumber, pdTemplate, pdDocument)
                    this.extractDiagramFromPage(dpr, pdTemplate)
                    this.clearTemplatePages()
                }
            }
        }
    }

    private fun clearTemplatePages() {
        for (i in (this.template.numberOfPages - 1) downTo 0) {
            template.removePage(i)
        }
    }

    /**
     * @param pageNumber 1-based
     */
    private fun importPageIntoTemplate(pageNumber: Int, pdTemplate: PDDocument, pdDocument: PDDocument){
        pdTemplate.importPage(pdDocument.getPage(pageNumber - 1))
    }

    private fun getCosIntegers(vararg nums: Long): Array<COSInteger> {
        return nums.map { COSInteger.get(it) }.toTypedArray()
    }

    private fun getPrependTokens(newHeight: Long?): List<Any> {
        val y = newHeight?.plus(72/2) ?: 842
        val q = Operator.getOperator("q")
        val cm = Operator.getOperator("cm")
        val g = Operator.getOperator("g")
        return listOf(
            q,
            *getCosIntegers(1, 0, 0, -1, 0, y), cm,
            q,
            *getCosIntegers(1, 0, 0, 1, 72, 0), cm,
            *getCosIntegers(0), g
        )
    }

    /**
     * todo: optimize? (lots of lines with lots of operators - buffer?, can be optimized later)
     * Algorithm:
     *   1. get diagram stream:
     *     1.1 read until first ET
     *     1.2 then buffer diagram operators until next BT or no more tokens (token == null)
     *   2. create pdf page with only the diagram operators using a template pdf created from the imported pdf
     *   3. crop the page
     *   4. and save it to a separate pdf file
     */
    private fun extractDiagramFromPage(dpage: DiagramPageResult, pdTemplate: PDDocument) {
        val pdfStreamParser = PDFStreamParser(dpage.pdPage)
        var token = pdfStreamParser.parseNextToken()
        while (!(token is Operator && token.name == "ET")) {
            token = pdfStreamParser.parseNextToken()
        }
        token = pdfStreamParser.parseNextToken()

        // using template, write diagram tokens to page stream
        val pdStream = PDStream(pdTemplate)
        val out = pdStream.createOutputStream()
        out.use {
            val tokenWriter = ContentStreamWriter(it)
            tokenWriter.writeTokens(getPrependTokens(dpage.searchedPage.diagramHeight?.toLong()))
            while (token != null && !(token is Operator && (token as Operator).name == "BT")) {
                tokenWriter.writeTokens(token)
                token = pdfStreamParser.parseNextToken()
            }
        }
        val page = pdTemplate.getPage(0)
        cropPage(page, dpage.searchedPage.diagramHeight)
        page.setContents(pdStream)
        savePdfPage(pdTemplate, getPdfFileName(dpage))
//        convertPageToSvg(pdTemplate, getSvgFileName(dpage))
    }

    private fun savePdfPage(pdTemplate: PDDocument, pdfFileName: String) {
        val newFilePath = resolveConflictsAndGetOutPath(pdfFileName) ?: return
        try {
            val pdfFile = Files.createFile(newFilePath).toFile()
            pdTemplate.save(pdfFile)
        } catch (e: Exception) {
            throw DiagramExtractorException("Failed to create or write to output file '${newFilePath.toFile().absolutePath}'", e)
        }
    }

    private fun convertPageToSvg(tempDoc: PDDocument, svgFileName: String) {
        val namespace = SVGDOMImplementation.SVG_NAMESPACE_URI
        val impl = SVGDOMImplementation.getDOMImplementation()
        val doc = impl.createDocument(namespace, "svg", null)
        val ctx = SVGGeneratorContext.createDefault(doc)
        ctx.isEmbeddedFontsOn = true
        val svgGraphics2D = SVGGraphics2D(ctx, false)
        val h = tempDoc.getPage(0).mediaBox.height.toInt() * 2
        val w = tempDoc.getPage(0).mediaBox.width.toInt() * 2
        svgGraphics2D.svgCanvasSize = Dimension(w, h)
        val pdfRenderer = PDFRenderer(tempDoc)
        svgGraphics2D.background = Color(255, 255, 255)
        pdfRenderer.renderPageToGraphics(0, svgGraphics2D, 2.0F)
        val root = svgGraphics2D.root
        root.setAttributeNS(null, "viewBox", "0 0 $w $h")
        writeToFile(svgGraphics2D, svgFileName, root)
    }

    private fun writeToFile(svgGraphics2D: SVGGraphics2D, svgFileName: String, root: Element) {
        val svgPath = resolveConflictsAndGetOutPath(svgFileName) ?: return
        try {
            val svgFile = Files.createFile(svgPath).toFile()
            svgFile.bufferedWriter().use {
                svgGraphics2D.stream(root, it, true, false)
            }
        } catch (e: Exception) {
            throw DiagramExtractorException("Failed to create or write to output file '${svgPath.toFile().absolutePath}'", e)
        }
    }

    private fun resolveConflictsAndGetOutPath(fileName: String): Path? {
        var svgPath: Path? = Paths.get(outputDir.absolutePath, fileName)
        val conflictingFile = findConflictingFile(outputDir.toPath(), svgPath!!)
        if (conflictingFile != null) {
            var choice = rememberChoicePdf
            if (choice == null) {
                val result = openFileExistsDialog(conflictingFile, svgPath)
                if (result.remember) rememberChoicePdf = result.choice
                choice = result.choice
            }
            when(choice) {
                FileConflictChoice.REPLACE_OR_MERGE -> {
                    deleteConflictingFile(conflictingFile.toFile())
                }
                FileConflictChoice.RENAME -> {
                    val newName = suggestName(outputDir.toPath(), svgPath)
                    svgPath = Paths.get(outputDir.absolutePath, newName)
                }
                FileConflictChoice.SKIP -> {
                    svgPath = null
                }
            }
        }
        return svgPath
    }

    private fun deleteConflictingFile(file: File): Boolean {
        try {
            return file.delete()
        } catch (e: SecurityException) {
            throw DiagramExtractorException("Failed to delete conflicting file '${file.name}' due to insufficient permissions.", e)
        } catch (e: Exception) {
            throw DiagramExtractorException("Failed to delete conflicting file '${file.name}'.", e)
        }
    }

    private fun getSvgFileName(dpage: DiagramPageResult): String {
        val paddedNumber = dpage.pageNumber.toString().padStart(this.paddingLength, '0')
        val diagramName = dpage.searchedPage.diagramName!!.replace(" ", "_")
        return "${this.fileName}_${paddedNumber}_$diagramName.svg"
    }

    private fun getPdfFileName(dpage: DiagramPageResult): String {
        val paddedNumber = dpage.pageNumber.toString().padStart(this.paddingLength, '0')
        val diagramName = dpage.searchedPage.diagramName!!.replace(" ", "_")
        return "${this.fileName}_${paddedNumber}_$diagramName.pdf"
    }

    private fun cropPage(page: PDPage, diagramHeight: Float?) {
        if (diagramHeight == null) return
        val newHeight = floor(diagramHeight + (72/2))
        val width = floor(page.mediaBox.width)
        val pdRectangle = PDRectangle(width, newHeight)
        page.mediaBox = pdRectangle
        page.bleedBox = pdRectangle
        page.cropBox = pdRectangle
    }

    private fun collectDiagramPages(document: PDDocument) {
        var pageIndex = 1
        for (page: PDPage in document.pages) {
            val searchedPage = searchPageForDiagramHeadings(document, pageIndex)
            if (searchedPage.isDiagramPage) {
                this.maxDiagramPageNumber = max(maxDiagramPageNumber, pageIndex)
                this.diagramPages.add(DiagramPageResult(pageIndex, page, searchedPage))
            }
            pageIndex += 1
        }
    }
}

