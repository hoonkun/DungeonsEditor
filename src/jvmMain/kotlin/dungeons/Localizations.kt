package dungeons

import LocalData
import androidx.compose.runtime.Stable
import utils.LocalizationResource

@Stable
class Localizations {

    companion object {

        val supported = listOf("en", "ko-KR")

        val Names = mapOf(
            "en" to "Language: English",
            "ko-KR" to "언어: 한국어"
        )

        private var _texts: Map<String, Map<String, String>>? = null
        private val texts get() = _texts!!

        operator fun get(key: String): String? {
            return texts[LocalData.locale]?.get(key)
        }

        private val ItemCorrections = mapOf(
            "CorruptedSeeds" to "CorruptedSeeds_Unique1"
        )

        val ItemNameCorrections = ItemCorrections + mapOf(
            "Powerbow" to "PowerBow",
            "Powerbow_Unique1" to "PowerBow_Unique1",
            "Powerbow_Unique2" to "PowerBow_Unique2"
        )

        val ItemFlavourCorrections = ItemCorrections + mapOf()
        val ItemDescriptionCorrections = ItemCorrections + mapOf()

        val ArmorPropertyCorrections = mapOf(
            "ItemCooldownDecrease" to "ArtifactCooldownDecrease",
            "ItemDamageBoost" to "ArtifactDamageBoost",
            "SlowResistance" to "FreezingResistance"
        )

        private val EnchantmentCorrections = mapOf(
            "VoidTouchedMelee" to "VoidStrikeMelee",
            "VoidTouchedRanged" to "VoidStrikeRanged",
            "CriticalHit" to "Critical",
            "FireAspect" to "Fire",
            "AnimaConduitMelee" to "Anima",
            "AnimaConduitRanged" to "AnimaRanged",
            "PoisonedMelee" to "Poisoned",
            "PoisonedRanged" to "Poisoned",
            "SoulSiphon" to "Soul",
            "Shockwave" to "Shock",
            "Gravity" to "GravityRanged",
            "MultiShot" to "Multi",
            "TempoTheft" to "Tempo"
        )

        val EnchantmentNameCorrections = EnchantmentCorrections + mapOf(
            "Deflecting" to "Deflect",
            "Celerity" to "Cool Down",
            "AnimaConduitRanged" to "Anima",
            "Accelerating" to "Accelerate",
            "EnigmaResonatorMelee" to "EnigmaMelee",
            "EnigmaResonatorRanged" to "EnigmaRanged",
        )

        val EnchantmentDescriptionCorrections = EnchantmentCorrections + mapOf(
            "ChainReaction" to "Chain",
            "EmeraldDivination" to "EmeraldDivination_effect",
            "Flee" to "Flee_effect",
            "DeathBarter" to "DeathBarter_effect",
            "Reckless" to "ShardArmor",
            "EnigmaResonatorMelee" to "Enigma",
            "EnigmaResonatorRanged" to "EnigmaRanged"
        )

        val EnchantmentFixedEffectCorrections = mapOf(
            "CriticalHit" to "Enchantment/label_chanceToTrigger",
            "PotionFortification" to "Enchantment/label_duration",
            "Rampaging" to "Enchantment/label_duration",
            "PoisonedMelee" to "Enchantment/label_damagePerSecond",
            "PoisonedRanged" to "Enchantment/label_damagePerSecond",
            "Stunning" to "Enchantment/label_chanceToTrigger",
            "Chains" to "Enchantment/label_duration",
            "MultiShot" to "Enchantment/label_chanceToTrigger",
            "Infinity" to "Enchantment/label_chanceToTrigger",
            "ChainReaction" to "Enchantment/label_chanceToTrigger",
            "WildRage" to "Enchantment/label_chanceToTrigger",
            "Ricochet" to "Enchantment/label_chanceToTrigger",
            "SurpriseGift" to "Enchantment/label_chanceToTrigger",
            "Deflecting" to "Enchantment/label_chanceToTrigger",
            "SpeedSynergy" to "Enchantment/label_duration",
            "SpiritSpeed" to "Enchantment/label_duration"
        )
        val EnchantmentEffectCorrections = EnchantmentCorrections + mapOf(
            "Gravity" to "GravityPulse",
            "GravityMelee" to "GravityPulse",
            "FireAspect" to "FireTrail",
            "ShadowFlash" to "shadowflash",
            "ShadowFeast" to "shadowfeast",
            "BagOfSouls" to "BagOfSoul",
            "EnigmaResonatorMelee" to "EnigmaMelee",
            "EnigmaResonatorRanged" to "EnigmaRanged",
        )

        private val EnUiTexts = mapOf(
            "season_limited" to "SeasonLimited",
            "change_type" to "Replace",
            "change_type_tooltip" to "Change this item to another",
            "transfer" to "Transfer",
            "pull" to "Pull",
            "transfer_tooltip" to "Transfer this item to {0}",
            "pull_tooltip" to "Pull this item from {0} to here({1})",
            "duplicate" to "Duplicate",
            "delete" to "Delete",
            "unknown_item" to "Unknown Item",
            "modified" to "Modified",
            "times" to "times",
            "melee" to "Melee",
            "armor" to "Armor",
            "ranged" to "Ranged",
            "artifact" to "Artifact",
            "armor_property" to "Armor Property",
            "close_file_title" to "Do you really want to finish editing and close file?",
            "close_file_description" to "Changes made after last save will not be applied",
            "file_load_src_title" to "Select file to edit!",
            "file_load_src_description" to "May be using another explorer to pasting path is more convenient",
            "file_load_failed_title" to "Failed to load file",
            "file_load_failed_description" to "Selected file may have invalid format, or developer's fault. Please file an issue!",
            "file_save_title" to "Select path to save!",
            "file_save_description" to "Selecting directory will save file in that location which have same name with input file,\nselecting file will save exactly in that file",
            "inventory_full_title" to "Inventory is too heavy to add new Item! Try move items to storage!",
            "inventory_delete_title" to "{0}Do you really want to delete this item?",
            "inventory_delete_title_arg" to "This item is in {0}. ",
            "inventory_delete_description" to "You can get emeralds if you salvage this in game, but deleting here can't.",
            "inventory_duplicate_title" to "This item is in {0}. Where do you want to dupliate this item?",
            "inventory_duplicate_description" to "You are viewing {0} now.",
            "inventory_duplicate_button_here" to "Here",
            "inventory_duplicate_button_source" to "Original Location",
            "close" to "Close",
            "ok" to "Ok",
            "cancel" to "Cancel",
            "save" to "Save",
            "progress_text_item_texture" to "Reading item textures",
            "progress_text_enchantment_texture" to "Reading enchantment textures",
            "progress_text_reading_pak" to "Reading .pak files",
            "progress_text_reading_localization" to "Reading localization files",
            "progress_text_reading_textures" to "Reading texture files",
            "progress_text_waiting" to "Waiting for previous work finishes",
            "progress_text_completed" to "Completed!",
            "cleaning_up" to "cleaning up",
            "pak_indexing_title" to "Reading game resources",
            "pak_indexing_description" to "This may take a bit, please wait until finishes!",
            "no_recent_files" to "No Recent Files!",
            "no_detected_files" to "No Detected Files!",
            "pak_not_found_title" to "Cannot find game resources.",
            "pak_not_found_description" to "Editor could not find game resources in predictable locations.\nPlease set game resources(.pak) directory manually.",
            "inventory" to "Inventory",
            "storage" to "Storage",
            "tap_to_close" to "Click anywhere to close",
            "select" to "select",
            "open" to "Open",
            "add" to "Add",
            "item_creation_other_options_description" to "You can edit other properties after adding this item!",
            "item_creation_armor_property_description" to "Default set of armor properties are manually hard coded, so it may not accurate.",
            "enchantment_unset" to "Set this slot as inactivated.\nApplying this to netherite enchantment will delete this item's glided property.",
            "enchantment_unset_effect" to "Do nothing in {0}% possibilities",
            "tips_title" to "Amateur Tips",
            "tips_0" to "You can display maximum 2 items in this area using LeftClick or RightClick",
            "tips_1" to "To modify inactive enchantments in activated slot, set activated enchantment's level to zero first.",
            "tips_2" to "To remove enchantment or armor property, click selected item in left list again.",
            "tips_3" to "To exit popup without any 'close' or 'cancel' button, click any position in window.",
            "tips_4" to "File selector, using another explorer to pasting path is more convenient",
            "tips_5" to "There is no guarantee with edit output file is playable in game, so MAKE BACKUPS PROPERLY.",
            "recent_files" to "Recent Files",
            "detected_files" to "Detected Files"
        )
        private val KoUiTexts = mapOf(
            "season_limited" to "시즌한정",
            "change_type" to "타입 변경",
            "change_type_tooltip" to "이 아이템을 다른 아이템으로 변경합니다",
            "transfer" to "{0}로 보내기",
            "pull" to "여기로 가져오기",
            "transfer_tooltip" to "이 아이템을 {0}로 보냅니다",
            "pull_tooltip" to "이 아이템을 {0}로부터 여기({1})로 가져옵니다",
            "duplicate" to "복제",
            "delete" to "삭제",
            "unknown_item" to "알 수 없는 아이템",
            "modified" to "효과 변경",
            "times" to "번",
            "melee" to "근거리",
            "armor" to "방어구",
            "ranged" to "원거리",
            "artifact" to "유물",
            "armor_property" to "방어구 속성",
            "close_file_title" to "정말 편집을 마치고 파일을 닫으시겠어요?",
            "close_file_description" to "마지막 저장 이후에 만든 변경사항은 저장되지 않아요",
            "file_load_src_title" to "수정할 파일을 선택해주세요!",
            "file_load_src_description" to "아마 다른 탐색기를 통해 경로를 복사하는게 더 편리할 수도 있어요...",
            "file_load_failed_title" to "파일 로드에 실패했어요",
            "file_load_failed_description" to "잘못된 파일이거나, 개발자가 이상한 짓을 해서 그럴 수도 있어요",
            "file_save_title" to "저장할 위치를 선택해주세요!",
            "file_save_description" to "디렉터리를 선택하면 기존 파일의 이름을 그대로 사용하여 선택한 디렉터리에 저장하고,\n그렇지 않을 경우 입력한 경로의 파일에 저장합니다.",
            "inventory_full_title" to "인벤토리가 가득 차서 더 이상 추가할 수 없어요. 먼저 아이템을 삭제하거나 창고로 옮겨보세요!",
            "inventory_delete_title" to "{0}정말 이 아이템을 삭제하시겠어요?",
            "inventory_delete_title_arg" to "{0}에 있는 아이템이에요. ",
            "inventory_delete_description" to "게임 내에서 분해하면 에메랄드 보상을 받을 수 있지만, 여기서는 받을 수 없어요",
            "inventory_duplicate_title" to "{0}에 있는 아이템이에요. 어디에 복제하시겠어요?",
            "inventory_duplicate_description" to "지금은 {0}를 보고있어요.",
            "inventory_duplicate_button_here" to "여기에 복제",
            "inventory_duplicate_button_source" to "원래 위치에 복제",
            "close" to "닫기",
            "ok" to "확인",
            "cancel" to "취소",
            "save" to "저장",
            "progress_text_item_texture" to "아이템 텍스쳐를 읽는 중입니다",
            "progress_text_enchantment_texture" to "효과 텍스쳐를 읽는 중입니다",
            "progress_text_reading_pak" to "Pak 파일을 읽는 중입니다",
            "progress_text_reading_localization" to "현지화 파일을 읽는 중입니다",
            "progress_text_reading_textures" to "텍스쳐 파일을 읽는 중입니다",
            "progress_text_waiting" to "이전 작업의 완료를 기다리고 있습니다",
            "progress_text_completed" to "완료되었습니다!",
            "cleaning_up" to "정리 중",
            "pak_indexing_title" to "게임 리소스를 읽는 중입니다",
            "pak_indexing_description" to "다소 시간이 걸릴 수 있으니 조금만 기다려주세요",
            "no_recent_files" to "최근 파일이 없어요!",
            "no_detected_files" to "탐지된 세이브파일이 없어요!",
            "pak_not_found_title" to "게임 리소스를 찾을 수 없습니다",
            "pak_not_found_description" to "예측 가능한 위치에서 게임파일을 찾지 못했습니다.\n게임 내 리소스 파일(.pak) 디렉터리의 경로를 입력해주세요.",
            "inventory" to "인벤토리",
            "storage" to "창고",
            "tap_to_close" to "닫으려면 아무 곳이나 누르세요",
            "select" to "선택",
            "open" to "열기",
            "add" to "추가",
            "item_creation_other_options_description" to "다른 옵션들은 추가한 뒤에 우측 영역에서 수정할 수 있어요!",
            "item_creation_armor_property_description" to "추가 후 표시되는 기본 ArmorProperty 값은 수기로 기록된 것으로, 정확하지 않을 수 있습니다.",
            "enchantment_unset" to "이 슬롯을 비활성화 상태로 변경합니다.\n'화려한'에 설정된 효과 부여의 경우 금박이 지워진 상태로 변경됩니다.",
            "enchantment_unset_effect" to "{0}% 확률로 아무것도 하지 않습니다?",
            "tips_title" to "아마추어 팁",
            "tips_0" to "이 영역에는 좌클릭/우클릭으로 최대 두 개의 아이템을 표시할 수 있습니다.",
            "tips_1" to "활성화된 효과부여 슬롯에서 나머지 비활성화된 슬롯을 수정하려면 먼저 활성화된 효과부여를 0레벨로 변경하여 비활성화합니다.",
            "tips_2" to "이미 추가된 효과, 방어구 속성을 삭제하려면 목록에서 선택된 항목을 다시 한 번 누릅니다.",
            "tips_3" to "대체로, 닫기 버튼이 없는 팝업 화면에서 빠져나가려면 주변의 빈 공간을 누르면 됩니다.",
            "tips_4" to "파일을 찾거나 저장할 때 보이는 파일 선택기에는 직접 입력하기보다는 기존 탐색기에서 경로를 복사해 붙혀넣는 것이 편리합니다.",
            "tips_5" to "수정 후 나온 결과물을 실제 게임 클라이언트가 받아줄 거라는 보장이 없으므로, 반드시 항상 백업을 만들어주세요.",
            "recent_files" to "Recent Files",
            "detected_files" to "Detected Files"
        )

        val UiTexts = mapOf(
            "en" to EnUiTexts,
            "ko-KR" to KoUiTexts
        )

        fun UiText(key: String, vararg args: String): String {
            var text = UiTexts.getValue(LocalData.locale)[key.lowercase()] ?: key
            args.forEachIndexed { index, s -> text = text.replace("{$index}", s) }
            return text
        }

        fun initialize() {
            val newTexts = mutableMapOf<String, Map<String, String>>()
            val languages = listOf("ko-KR", "en")
            languages.forEach {
                newTexts[it] = LocalizationResource.read(PakRegistry.index.getFileBytes("/Dungeons/Content/Localization/Game/$it/Game.locres")!!)
            }
            _texts = newTexts
        }

    }

}