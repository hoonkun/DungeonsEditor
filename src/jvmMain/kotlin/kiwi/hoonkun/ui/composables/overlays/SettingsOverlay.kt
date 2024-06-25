package kiwi.hoonkun.ui.composables.overlays

import LocalWindowState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.FileSelector
import kiwi.hoonkun.ui.composables.base.RetroButton
import kiwi.hoonkun.ui.composables.base.RetroButtonHoverInteraction
import kiwi.hoonkun.ui.reusables.handCursor
import kiwi.hoonkun.ui.reusables.rememberMutableInteractionSource
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import java.io.File

@Composable
fun SettingsOverlay() {
    val scope = rememberCoroutineScope()
    val windowState = LocalWindowState.current

    ContentRoot {
        OverlayTitleDescription(title = Localizations.UiText("settings_title"))
        Spacer(modifier = Modifier.height(36.dp))

        SettingsRow {
            SettingLabel(text = Localizations.UiText("settings_lang"))
            Spacer(modifier = Modifier.weight(1f))
            SelectableRetroButton(
                text = "한국어",
                modifier = Modifier.width(128.dp).fillMaxHeight(),
                onClick = { ArcticSettings.withSave { locale = "ko-KR" } },
                selected = ArcticSettings.locale == "ko-KR"
            )
            Spacer(modifier = Modifier.width(16.dp))
            SelectableRetroButton(
                text = "English",
                modifier = Modifier.width(128.dp).fillMaxHeight(),
                onClick = { ArcticSettings.withSave { locale = "en" } },
                selected = ArcticSettings.locale == "en"
            )
        }
        SettingsRow {
            SettingLabel(text = Localizations.UiText("settings_global_scale"))
            Spacer(modifier = Modifier.weight(1f))
            ScaleCandidates.forEach { (name, value) ->
                Spacer(modifier = Modifier.width(16.dp))
                SelectableRetroButton(
                    text = "x${name}",
                    modifier = Modifier.width(96.dp).fillMaxHeight(),
                    onClick = { ArcticSettings.withSave { globalScale = value } },
                    selected = ArcticSettings.globalScale == value,
                )
            }
        }
        SettingsRow {
            SettingLabel(text = Localizations.UiText("settings_preload_textures"))
            Spacer(modifier = Modifier.weight(1f))
            SettingBooleanValue(
                text =
                if (ArcticSettings.preloadTextures) Localizations.UiText("settings_on")
                else Localizations.UiText("settings_off"),
                selected = ArcticSettings.preloadTextures,
                onSelectedChange = { ArcticSettings.withSave { preloadTextures = it } }
            )
        }
        SettingsColumn {
            SettingLabel(
                text = Localizations.UiText("settings_pak_location"),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val existingCustomPakLocation = remember { ArcticSettings.customPakLocation }
            FileSelector(
                buttonText = Localizations.UiText("select"),
                validator = { it.isDirectory && it.listFiles()?.any { file -> file.extension == "pak" } == true },
                onSelect = { ArcticSettings.withSave { customPakLocation = it.absolutePath } },
                initialPath = existingCustomPakLocation ?: File.separator,
                initialUseBasePath = existingCustomPakLocation == null,
                maxRows = 1
            )
        }
    }
}

private val ScaleCandidateNames = listOf("0.6", "0.8", "1", "1.2", "1.4")
private val ScaleCandidateValues = listOf(0.4f, 0.475f, 0.55f, 0.625f, 0.7f)
private val ScaleCandidates = ScaleCandidateNames.zip(ScaleCandidateValues)

@Composable
private fun SelectableRetroButton(
    text: String,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(if (selected) Color(0xff3f8e4f) else Color(0xff343434))

    RetroButton(
        text = text,
        color = { color },
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun SettingLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = LocalTextStyle.current.copy(fontSize = 24.sp), modifier = modifier)
}

private val ContentWidth get() = 800.dp

@Composable
private fun SettingsRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 24.dp).requiredSize(ContentWidth, 56.dp),
        content = content
    )
}

@Composable
private fun SettingsColumn(
    content: @Composable ColumnScope.() -> Unit
) {
    Column (
        modifier = Modifier.padding(bottom = 24.dp).requiredWidth(ContentWidth),
        content = content
    )
}

@Composable
private fun SettingBooleanValue(
    text: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
) {
    Text(
        text = text,
        style = LocalTextStyle.current.copy(
            fontSize = 24.sp,
            color = if (selected) Color(0xffcc7832) else Color(0xff646464)
        ),
        modifier = Modifier
            .clickable(rememberMutableInteractionSource(), null) { onSelectedChange(!selected) }
            .handCursor()
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .offset(x = 16.dp)
    )
}
