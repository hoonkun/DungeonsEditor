package kiwi.hoonkun.resources

import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.utils.resourceText
import kotlinx.serialization.json.Json

object Localizations {

    val supported = listOf("en", "ko-KR")

    private val texts = mapOf(
        "en" to Json.decodeFromString<Map<String, String>>(resourceText("localizations/ui_en_US.json")),
        "ko-KR" to Json.decodeFromString<Map<String, String>>(resourceText("localizations/ui_ko_KR.json")),
    )

    operator fun get(key: String, vararg args: String): String =
        args.foldIndexed(texts.getValue(ArcticSettings.locale)[key.lowercase()] ?: key) { index, acc, s ->
            acc.replace("{$index}", s)
        }

}