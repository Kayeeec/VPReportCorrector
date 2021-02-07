package org.vpreportcorrector.app
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        // Classes
        val heading by cssclass()
        val sideButton by cssclass()

        val h1 by cssclass()
        val h2 by cssclass()
        val h3 by cssclass()
        val p by cssclass()
        val paddedContainer by cssclass()
        val centered by cssclass()
        val typographyText by cssclass()

        val icon48 by cssclass()
        val iconBlue by cssclass()

        val helpLabel by cssclass()
        val helpIcon by cssclass()

//        val filePdfIcon by cssclass()

        // Properties
        val iconColor by cssproperty<Paint>("-fx-icon-color")

        // Colors
        val colorInfo = c("#6495ED")
        val colorInfoMuted = c("#6495ED", 0.8)
        val colorSecondary = c("#6c757d")
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

        menuBar {
            padding = box(0.px)
        }

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

        paddedContainer {
            padding = box(10.px)
        }

        centered {
            textAlignment = TextAlignment.CENTER
            typographyText {
                textAlignment = TextAlignment.CENTER
            }
        }

        icon48 {
            fontSize = 48.px
        }

        iconBlue {
            iconColor.value = colorInfo
        }

        helpLabel {
            padding = box(2.px, 2.px, 2.px, 5.px)
        }

        helpIcon {
            fontSize = 20.px
            iconColor.value = colorInfoMuted
        }

        s(".split-pane-divider") {
            padding = box(0.px, 1.px, 0.px, 0.px)
            borderWidth += box(0.px, 1.px, 0.px, 1.px)
        }
    }
}
