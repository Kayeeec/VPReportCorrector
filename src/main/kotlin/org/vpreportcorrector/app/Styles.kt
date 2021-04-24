package org.vpreportcorrector.app
import javafx.geometry.Pos
import javafx.scene.layout.HBox
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
        val flatButton by cssclass()

        val diagramAnnotatorView by cssclass()
        val diagramErrorsView by cssclass()
        val issueChooserFragment by cssclass()
        val issueGroupCheckboxes by cssclass()

        val treeToolbar by cssclass()

        val h1 by cssclass()
        val h2 by cssclass()
        val h3 by cssclass()
        val h4 by cssclass()
        val p by cssclass()
        val paddedContainer by cssclass()
        val centered by cssclass()
        val typographyText by cssclass()
        val textMuted by cssclass()
        val textError by cssclass()
        val textBold by cssclass()

        val icon48 by cssclass()
        val iconBlue by cssclass()

        val helpLabel by cssclass()
        val helpIcon by cssclass()

//        val filePdfIcon by cssclass()

        // Properties
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

        menuBar {
            padding = box(0.px)
            backgroundColor += AppColors.bgPrimary
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

        h4 {
            fontSize = 14.px
            fontWeight = FontWeight.BOLD
            padding = box(10.px, 0.px, 5.px, 0.px)
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

        textMuted {
            fill = AppColors.textMuted
            text {
                fill = AppColors.textMuted
            }
        }

        textError {
            fill = AppColors.textError
            text {
                fill = AppColors.textError
            }
            iconColor.value = AppColors.textError
        }
        textBold {
            fontWeight = FontWeight.BOLD
            text {
                fontWeight = FontWeight.BOLD
            }
        }

        icon48 {
            fontSize = 48.px
        }

        iconBlue {
            iconColor.value = AppColors.textInfo
        }

        helpLabel {
            padding = box(2.px, 2.px, 2.px, 5.px)
        }

        helpIcon {
            fontSize = 20.px
            iconColor.value = AppColors.textInfoMuted
        }

        s(".split-pane-divider") {
            padding = box(0.px, 1.px, 0.px, 0.px)
            borderWidth += box(0.px, 1.px, 0.px, 1.px)
        }

        diagramErrorsView {
            title {
                backgroundColor += AppColors.bgPrimary
                alignment = Pos.CENTER_LEFT
                label {
                    fontSize = 13.px
                    fontWeight = FontWeight.BOLD
                    padding = box(0.px, 0.px, 0.px, 10.px)
                }
                button {
                    fontWeight = FontWeight.NORMAL
                }
            }
            backgroundColor += AppColors.bgSecondary
            checkBox {
                padding = box(8.px, 10.px)
                alignment = Pos.TOP_LEFT
                opacity = 1.0
                and(selected) {
                    text {
                        fontWeight = FontWeight.EXTRA_BOLD
                    }
                }
                and(hover) {
                    backgroundColor += AppColors.bgButtonHover
                }
            }
            form {
                backgroundColor += AppColors.bgSecondary
            }
            Stylesheet.titledPane {
                title {
                    backgroundColor += AppColors.bgPrimary
                }
                content {
                    backgroundColor += AppColors.bgSecondary
                }
            }
        }

        flatButton {
            backgroundColor += Color.TRANSPARENT
            borderWidth += box(0.px)
            borderRadius += box(0.px)
            backgroundRadius += box(0.px)
            and(hover) {
                backgroundColor += AppColors.bgButtonHover
            }
            and(selected) {
                backgroundColor += AppColors.bgButtonActive
            }
        }

        diagramAnnotatorView {
            flatButton {
                fontSize = 14.px
            }
        }

        issueChooserFragment {
            radioButton {
                padding = box(0.px, 0.px, 8.px, 0.px)
            }
            issueGroupCheckboxes {
                checkBox {
                    padding = box(0.px, 0.px, 8.px, 0.px)
                    and(selected) {
                        text {
                            fontWeight = FontWeight.EXTRA_BOLD
                        }
                    }
                }
                backgroundColor += c("white")
                padding = box(10.px)
            }
        }

        treeToolbar {
            title {
                backgroundColor += AppColors.bgPrimary
                alignment = Pos.CENTER_LEFT
                label {
                    fontSize = 13.px
                    fontWeight = FontWeight.BOLD
                }
                button {
                    fontWeight = FontWeight.NORMAL
                }
            }
        }


    }
}

class AppColors() {
    companion object {
        val textError = c("#B71C1C")
        val textInfo = c("#6495ED")
        val textInfoMuted = c("#6495ED", 0.8)
        val textSecondary = c("#6c757d")
        val textMuted = c("#6c757d")
        val bgPrimary = c("#f4f4f4")
        val bgPrimaryLighter = c("#f9f9f9")
        val bgSecondary = c("#ffffff")
        val bgButtonHover = c("#e1e2e1")
        val bgButtonActive = c("#c1c1c1")
        val borderGray = c("#c8c8c8")

    }
}
