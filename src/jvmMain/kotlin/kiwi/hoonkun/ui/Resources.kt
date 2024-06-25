package kiwi.hoonkun.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import javax.imageio.ImageIO

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

        val settings = useResource("settings.png") { ImageIO.read(it).toComposeImageBitmap() }

        val leave = useResource("leave.png") { ImageIO.read(it).toComposeImageBitmap() }
    }

}