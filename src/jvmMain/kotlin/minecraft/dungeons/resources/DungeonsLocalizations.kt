package minecraft.dungeons.resources

import kiwi.hoonkun.ArcticSettings
import minecraft.dungeons.io.DungeonsPakRegistry
import utils.LocalizationResource

object DungeonsLocalizations {
    private var texts: MutableMap<String, Map<String, String>> = mutableMapOf()

    operator fun get(key: String): String? =
        texts.getValue(ArcticSettings.locale)[key]

    fun initialize() {
        listOf("ko-KR", "en").forEach {
            val bytes = DungeonsPakRegistry.index.getFileBytes("/Dungeons/Content/Localization/Game/$it/Game.locres")
            texts[it] = LocalizationResource.read(bytes!!)
        }
    }
}