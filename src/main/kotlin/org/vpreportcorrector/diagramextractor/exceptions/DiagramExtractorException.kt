package org.vpreportcorrector.diagramextractor.exceptions

class DiagramExtractorException(override val message: String?, throwable: Throwable?): Exception(message, throwable)
