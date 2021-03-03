package org.vpreportcorrector.mainview

import javafx.beans.property.SimpleObjectProperty
import org.vpreportcorrector.utils.KEY_WORKING_DIRECTORY
import tornadofx.ItemViewModel
import java.io.File
import tornadofx.getValue
import tornadofx.setValue
import java.lang.Exception
import java.nio.file.Paths

class GlobalData {
    val workingDirectoryProperty = SimpleObjectProperty<File>(null)
    var workingDirectory by workingDirectoryProperty
}


class GlobalDataModel : ItemViewModel<GlobalData>() {
    val workingDirectory = bind(GlobalData::workingDirectoryProperty)

    fun loadPreferencesData(){
        var workdirstr = ""
        preferences {
            sync()
            workdirstr = get(KEY_WORKING_DIRECTORY, "")
        }
        if (workdirstr.isNotBlank()) {
            try {
                workingDirectory.value = Paths.get(workdirstr).toFile()
            } catch (e: Exception) { log.severe(e.stackTraceToString()) }
        }
    }
}




