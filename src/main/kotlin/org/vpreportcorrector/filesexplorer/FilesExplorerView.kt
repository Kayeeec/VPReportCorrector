package org.vpreportcorrector.filesexplorer

import tornadofx.*

class FilesExplorerView : View("Working directory") {

    override val root = vbox {
        hbox {
            button("R")
            button("N")

        }
        vbox {
            text("list items")
        }
    }
}
