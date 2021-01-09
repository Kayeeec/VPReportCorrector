package org.vpreportcorrector.filesexplorer

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class NewFolderDialogView : View("New folder") {
    val model: NewFolderModel by inject()

    override val root = form {
        fieldset {
            field("Location:") {
                textfield(model.location) {
                    required()
                    tooltip(model.location.value)
                }
                // TODO: 07.01.21 fix style: btn unaligned
                button("", FontIcon(FontAwesomeSolid.FOLDER_OPEN)) {
                    tooltip("Choose new folder location")
                    action {
                        val loc = chooseDirectory("Choose new folder location", File(model.location.value))
                        val absolutePath = if (loc !== null && loc.isDirectory) loc.absolutePath else null
                        model.location.value = absolutePath
                        // TODO: 07.01.21 validate location (not above root)
                    }
                }
            }
            field("New folder name:") {
                textfield(model.name) {
                    required()
                    whenDocked { requestFocus() }
                }
            }
            buttonbar {
                button("Cancel"){
                    action { close() }
                }
                button("Save") {
                    enableWhen(model.valid)
                    action {
                        model.commit()
                        val newFolder = model.item
                        // TODO: 07.01.21 error hadling
                        var path = newFolder.location
                        if (!path.endsWith("/")){
                            path += "/"
                        }
                        path += newFolder.name
                        Files.createDirectory(Paths.get(path))
                        close()
                    }
                }
            }
            model.validate(decorateErrors = false)
        }
    }
}
