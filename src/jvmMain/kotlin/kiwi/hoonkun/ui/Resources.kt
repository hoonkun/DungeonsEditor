package kiwi.hoonkun.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

object Resources {

    object Fonts {

        val JetbrainsMono =
            FontFamily(
                Font(
                    resource = "JetBrainsMono-Regular.ttf",
                    weight = FontWeight.W400,
                    style = FontStyle.Normal
                )
            )

    }

    object Drawables {

        @Composable
        fun icon() = painterResource("_icon.png")

        @Composable
        fun detectedIcon() = painterResource("main_icon_detected_files.svg")


        @Composable
        fun historyIcon() = painterResource("main_icon_history.svg")

    }

}