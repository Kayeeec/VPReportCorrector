package org.vpreportcorrector.diagramextractor

import org.vpreportcorrector.utils.FileConflictChoice
import java.io.File

/**
 * This class serves as a blueprint for implementing diagram extraction.
 *
 * @property inputFile a file to extract diagrams from
 * @property outputDirectory a directory location to save the extracted diagram files to
 * @property savedFileConflictChoice an enum, which signifies what to do when a conflicting file should be created. Can be specified later during the computation.
 *
 * @constructor Creates an extractor class instance ready for diagram extraction, with the inputFile and outputDirectory specified.
 */
abstract class DiagramExtractor(
    inputFile: File,
    open val outputDirectory: File,
    open var savedFileConflictChoice: FileConflictChoice?
) {
    /**
     * Extracts diagrams as files from an input file into an output directory specified in this DiagramExtractor.
     */
    abstract fun extractDiagrams()
}
