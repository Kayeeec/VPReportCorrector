package org.vpreportcorrector.mergepdfs

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import tornadofx.asObservable
import tornadofx.getValue
import tornadofx.setValue
import java.io.File


class MergePdfsModel(destinationFile: File? = null, pdfFiles: List<File> = mutableListOf()) {
    val pdfFilesProperty = SimpleListProperty(this, "pdfFiles", pdfFiles.asObservable())
    var pdfFiles: ObservableList<File> by pdfFilesProperty

    val destinationFileProperty = SimpleObjectProperty<File>(this, "destinationFile", destinationFile)
    var destinationFile: File? by destinationFileProperty

    /**
     * Merges PDF files in [pdfFiles] into a single PDF file denoted by [destinationFile].
     * Preserves order from [pdfFiles].
     * Creates the [destinationFile] if it does not exist, overrides it if it does.
     */
    fun merge() {
        if (destinationFile == null) return
        val merger = PDFMergerUtility()
        merger.destinationFileName = destinationFile!!.absolutePath
        pdfFiles.forEach {
            merger.addSource(it)
        }
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())
    }
}
