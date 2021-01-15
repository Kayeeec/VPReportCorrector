package types

import org.apache.pdfbox.text.TextPosition

class TextPositionSequence(
    private var textPositions: List<TextPosition>,
    private var start:  Int,
    private var end: Int,
) : CharSequence {
    constructor(textPositions: List<TextPosition>): this(textPositions, 0, textPositions.size)

    override val length: Int
        get() = this.end - this.start

    val fontSize: Float?
        get() = if (this.textPositions.isNotEmpty()) this.textPositionAt(0).fontSize else null

    /**
     * renamed from charAt
     */
    override fun get(index: Int): Char {
        val text: String = textPositionAt(index).unicode
        return text[0]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): TextPositionSequence {
        return TextPositionSequence(textPositions, this.start + startIndex, this.start + endIndex)
    }

    override fun toString(): String {
        val builder = StringBuilder(length)
        for (i in 0 until length) {
            builder.append(get(i))
        }
        return builder.toString()
    }

    private fun textPositionAt(index: Int): TextPosition {
        return this.textPositions[this.start + index]
    }

    val yDirAdj: Float
        get() = textPositions[start].yDirAdj
}
