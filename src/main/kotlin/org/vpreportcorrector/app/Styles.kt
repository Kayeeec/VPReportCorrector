package org.vpreportcorrector.app
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val sideButton by cssclass()
//        val filePdfIcon by cssclass()
//        val iconColor by cssproperty<Paint>("-fx-icon-color")

    }

    init {
        label and heading {
            padding = box(10.px)
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }

//        s(".file-pdf-icon .ikonli-font-icon") {
//            iconColor.value = c("#b32015")
//        }

        sideButton {
            fontSize = 10.px

            tooltip {
                fontSize = 12.px
            }
        }
    }
}
