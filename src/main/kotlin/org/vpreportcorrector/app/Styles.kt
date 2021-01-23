package org.vpreportcorrector.app
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val sideButton by cssclass()

        val h1 by cssclass()
        val h2 by cssclass()
        val h3 by cssclass()
        val p by cssclass()
        val container by cssclass()

        val icon48 by cssclass()
        val iconBlue by cssclass()

//        val filePdfIcon by cssclass()
        val iconColor by cssproperty<Paint>("-fx-icon-color")

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

        h1 {
            fontSize = 30.px
            padding = box(20.px, 0.px, 10.px, 0.px)
        }

        h2 {
            fontSize = 24.px
            padding = box(20.px, 0.px, 10.px, 0.px)
        }

        h3 {
            fontSize = 21.px
            fontWeight = FontWeight.BOLD
            padding = box(15.px, 0.px, 10.px, 0.px)
        }

        p {
            fontSize = 14.px
            padding = box(0.px, 0.px, 10.px, 0.px)
        }

        container {
            padding = box(10.px)
        }

        icon48 {
            fontSize = 48.px
        }

        iconBlue {
            iconColor.value = Color.CORNFLOWERBLUE
        }


    }
}
