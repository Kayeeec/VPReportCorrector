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
import types.DiagramPageResult
import utils.searchPageForDiagramHeadings
import java.awt.Color
import java.io.File
import kotlin.math.max

class DiagramExtractor(
    private val filePath: String,
    private val outPath: String = "extracted_diagrams", // todo: will be defined by user (during GUI implementation)
) {
    private val diagramPages = mutableListOf<DiagramPageResult>()
    private var maxDiagramPageNumber = 0
    private var fileName: String
    private var document: PDDocument
    private var template: PDDocument
    private val paddingLength: Int
        get() = "${this.maxDiagramPageNumber}".length

    init {
        val file = File(this.filePath)
        this.document = PDDocument.load(file) // TODO: 27.10.20 handle IO (during GUI implementation)
        this.fileName = file.nameWithoutExtension
        this.template = PDDocument.load(file) // TODO: 27.10.20 handle IO (during GUI implementation)
    }

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
     *   4. convert it to svg
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
        convertPageToSvg(pdTemplate, getSvgFileName(dpage))
    }

    private fun convertPageToSvg(tempDoc: PDDocument, svgFileName: String) {
        val namespace = SVGDOMImplementation.SVG_NAMESPACE_URI
        val impl = SVGDOMImplementation.getDOMImplementation()
        val doc = impl.createDocument(namespace, "svg", null)
        val ctx = SVGGeneratorContext.createDefault(doc)
        ctx.isEmbeddedFontsOn = true
        val svgGraphics2D = SVGGraphics2D(ctx, false)
        val pdfRenderer = PDFRenderer(tempDoc)
        svgGraphics2D.background = Color(255, 255, 255)
        pdfRenderer.renderPageToGraphics(0, svgGraphics2D, 2.0F)

        // todo: more resilient file creation IO, ensure outPath never + no end slash (during GUI impl.)
        val svgFile = File("${this.outPath}/${svgFileName}")
        svgFile.parentFile.mkdirs()
        svgFile.setExecutable(false)
        svgFile.bufferedWriter().use {
            svgGraphics2D.stream(it)
        }
    }

    private fun getSvgFileName(dpage: DiagramPageResult): String {
        val paddedNumber = dpage.pageNumber.toString().padStart(this.paddingLength, '0')
        val diagramName = dpage.searchedPage.diagramName!!.replace(" ", "_")
        return "${this.fileName}_${paddedNumber}_$diagramName.svg"
    }

    private fun cropPage(page: PDPage, diagramHeight: Float?) {
        if (diagramHeight == null) return
        val newHeight = diagramHeight + (72/2)
        val width = page.mediaBox.width
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

