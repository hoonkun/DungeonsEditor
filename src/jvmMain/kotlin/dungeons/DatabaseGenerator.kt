package dungeons

import pak.PakIndex
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.pathString

class DatabaseGenerator {

    companion object {

        private val keyMappings: Map<String, String> = mapOf(
            "TrickBow" to "Trickbow",
            "TrickBow_Unique1" to "Trickbow_Unique1",
            "TrickBow_Unique2" to "Trickbow_Unique2",
            "TrickBow_Year1" to "Trickbow_Year1",
            "LongBow" to "Longbow",
            "LongBow_Unique1" to "Longbow_Unique1",
            "LongBow_Unique2" to "Longbow_Unique2",
            "PowerBow" to "Powerbow",
            "PowerBow_Unique1" to "Powerbow_Unique1",
            "PowerBow_Unique2" to "Powerbow_Unique2",
            "Slowbow_Unique1" to "SlowBow_Unique1",
            "ShortBow" to "Shortbow",
            "ShortBow_Unique1" to "Shortbow_Unique1",
            "ShortBow_Unique2" to "Shortbow_Unique2",
            "ShortBow_Unique3" to "Shortbow_Unique3",
            "Huntingbow_Unique1" to "HuntingBow_Unique1",
            "TwistingVineBow_UNique1" to "TwistingVineBow_Unique1",

            "Battlerobe_unique1" to "BattleRobe_Unique1",

            "Sword_Steel" to "Sword",
            "Pickaxe_Steel" to "Pickaxe",
            "Pickaxe_Unique1_Steel" to "Pickaxe_Unique1",
            "Daggers_unique2" to "Daggers_Unique2",

            "Beenest" to "BeeNest",
            "SatchelofNourishment" to "SatchelOfNourishment",
            "SatchelofNeed" to "SatchelOfNeed",

            "Firetrail" to "FireTrail"
        )

        private val itemsExcluded: List<String> = listOf(
            "MysteryBox", "Potions", "Elytra", "EyeOfEnder", "Arrow_Icon", "TNTBox", "Trident", "HighlanderLongSword",
            "DenseBrew", "WaterBreathingPotion", "HealthPotion", "StrengthPotion", "SwiftnessPotion", "BurningBrewPotion", "OakWoodBrew", "BackstabbersBrew", "Arrow", "SweetBrew",
            "Food1", "Food2", "Food3", "Food4", "Food5", "Food6", "Food7", "Food8",
            "MysteryBoxRanged", "MysteryBoxMelee", "MysteryBoxArmor", "MysteryBoxArtifact", "MysteryBoxAny"
        )

        private val PowerfulEnchantments = setOf(
            "GravityMelee", "Swirling", "VoidTouchedMelee", "Exploding", "RadianceMelee", "Shockwave", "CriticalHit", "PotionThirstMelee", // Melee
            "GravityPulse", "FinalShout", "Chilling", "Protection", "DeathBarter", "MultiDodge", "PoisonFocus", "SoulFocus", "FireFocus", "Reckless", // Armors
            "TempoTheft", "LevitationShot", "ChainReaction", "VoidTouchedRanged", "Gravity", "MultiCharge", "PotionThirstRanged", "ShockWeb" // Ranged
        )

        private val ArmorEnchantments = setOf(
            "Swiftfooted", "PotionFortification", "Snowing", "SurpriseGift", "Burning", "Cowardice", "Deflecting",
            "Electrified", "Thorns", "Explorer", "Frenzied", "Celerity", "Recycler", "FoodReserves", "FireTrail",
            "HealthSynergy", "SpeedSynergy", "SpiritSpeed", "FinalShout", "Chilling", "Protection", "GravityPulse",
            "Acrobat", "TumbleBee", "BagOfSouls", "LuckOfTheSea", "EmeraldDivination", "MultiDodge", "ResurrectionSurge",
            "DeathBarter", "Flee", "ShadowFlash", "ShadowFeast", "FireFocus", "BeastBoss", "PoisonFocus", "SoulFocus",
            "Reckless", "BeastSurge", "BeastBurst", "LightningFocus"
        )

        private val MeleeEnchantments = setOf(
            "Weakening", "FireAspect", "Looting", "Chains", "Echo", "Stunning", "Rampaging", "AnimaConduitMelee",
            "Freezing", "Committed", "PoisonedMelee", "Prospector", "EnigmaResonatorMelee", "SoulSiphon", "Thundering",
            "Sharpness", "Leeching", "CriticalHit", "Exploding", "RadianceMelee", "GravityMelee", "Shockwave", "Swirling",
            "DynamoMelee", "BusyBee", "Smiting", "VoidTouchedMelee", "BaneOfIllagers", "GuardingStrike", "PotionThirstMelee",
            "Backstabber", "DamageSynergy", "PainCycle", "Unchanting"
        )

        private val RangedEnchantments = setOf(
            "Accelerating", "Growing", "AnimaConduitRanged", "RapidFire", "BurstBowstring", "DynamoRanged",
            "Infinity", "Unchanting", "Piercing", "Power", "WildRage", "Punch", "Ricochet", "Supercharge", "FuseShot",
            "BonusShot", "FireAspect", "MultiShot", "Gravity", "TempoTheft", "ChainReaction", "RadianceRanged",
            "SmitingRanged", "PoisonedRanged", "LevitationShot", "EnigmaResonatorRanged", "VoidTouchedRanged",
            "RollCharge", "MultiCharge", "PotionThirstRanged", "DippingPoison", "CooldownShot", "ShockWeb", "ArtifactCharge",
            "Exploding"
        )

        private val ExclusiveEnchantments = mapOf(
            "JunglePoisonMelee" to listOf("Anchor_Unique1", "Whip_Unique1"),
            "JunglePoisonRanged" to listOf(),
            "ShadowShot" to listOf("Shadow_Crossbow", "Veiled_Crossbow", "BatCrossbow"),
            "ThriveUnderPressure" to listOf("Shulker_Armor", "Shulker_Armor_Unique1"),
            "SharedPain" to listOf("ObsidianClaymore_Unique1"),
            "Rushdown" to listOf("TempestKnife", "TempestKnife_Unique2", "TempestKnife_Unique1"),
            "ReliableRicochet" to listOf("BubbleBow_Unique1", "BubbleBow_Spooky2")
        )

        private val MultipleAllowedEnchantments = setOf(
            "Chains", "FireAspect", "Leeching", "Looting", "ProspectorMelee", "RadianceMelee", "Rampaging", "Sharpness",
            "Smiting", "Weakening", "Celerity", "Thorns"
        )

        private val EnchantmentSpecialDescValue = mapOf(
            "CriticalHit" to listOf("ValuesFormat/multiplenoun_3")
        )

        private val armorProperties = setOf(
            ArmorPropertyData("AllyDamageBoost", listOf(
                "MercenaryArmor", "SpelunkersArmor", "SpelunkersArmor_Unique1", "WolfArmor", "WolfArmor_Unique1", "WolfArmor_Unique2",
                "DarkArmor_Unique1", "WolfArmor_Winter1", "SpelunkersArmor_Year1", "MercenaryArmor_Spooky2"
            )),
            ArmorPropertyData("AreaHeal", listOf(
                "BardsGarb", "BardsGarb_Unique1", "NatureArmor", "NatureArmor_Unique1", "SproutArmor", "SproutArmor_Unique1",
                "WolfArmor", "WolfArmor_Unique1", "WolfArmor_Unique2", "ChampionsArmor_Unique1", "BeenestArmor", "BeenestArmor_Unique1", "WolfArmor_Winter1"
            )),
            ArmorPropertyData("DamageAbsorption", listOf()), // Built-In 으로 쓰이는 곳이 있는건가?
            ArmorPropertyData("DodgeCooldownIncrease", listOf("ReinforcedMail", "ReinforcedMail_Unique1")),
            ArmorPropertyData("DodgeInvulnerability", listOf("OcelotArmor_Unique1", "SquidArmor_Unique1")),
            ArmorPropertyData("DodgeSpeedIncrease", listOf("OcelotArmor", "OcelotArmor_Unique1")),
            ArmorPropertyData("HealingAura", listOf("TurtleArmor", "TurtleArmor_Unique1")),
            ArmorPropertyData("IncreasedArrowBundleSize", listOf("CowardsArmor", "ArchersStrappings", "ArchersStrappings_Unique1")),
            ArmorPropertyData("IncreasedMobTargeting", listOf("ChampionsArmor", "ChampionsArmor_Unique1", "ShulkerArmor", "ShulkerArmor_Unique1")),
            ArmorPropertyData("ItemCooldownDecrease", listOf(
                "ClimbingGear", "ClimbingGear_Unique1", "ClimbingGear_Unique2", "EvocationRobe", "EvocationRobe_Unique1",
                "EvocationRobe_Unique2", "CowardsArmor", "BattleRobe", "BattleRobe_Unique1",
            )),
            ArmorPropertyData("ItemDamageBoost", listOf(
                "PiglinArmor", "PiglinArmor_Unique1", "SoulRobe", "SoulRobe_Unique1", "BattleRobe_Unique1", "SpelunkersArmor_Unique1", "SpelunkersArmor_Year1"
            )),
            ArmorPropertyData("LifeStealAura", listOf("GrimArmor", "GrimArmor_Unique1", "AssassinArmor_Unique1", "GrimArmor_Spooky2")),
            ArmorPropertyData("MeleeAttackSpeedBoost", listOf(
                "EmeraldArmor", "EmeraldArmor_Unique1", "EmeraldArmor_Unique2", "AssassinArmor_Unique1", "AssassinArmor"
            )),
            ArmorPropertyData("MeleeDamageBoost", listOf("ScaleMail", "ScaleMail_Unique1", "FullPlateArmor_Unique1", "BattleRobe", "BattleRobe_Unique1", "FullPlateArmor_Spooky2")),
            ArmorPropertyData("MissChance", listOf(
                "ReinforcedMail", "ReinforcedMail_Unique1", "SoulRobe_Unique1", "WolfArmor_Unique1", "FullPlateArmor", "FullPlateArmor_Unique1",
                "GhostArmor", "GhostArmor_Unique1", "WolfArmor_Winter1", "FullPlateArmor_Spooky2", "GhostArmor_Spooky2"
            )),
            ArmorPropertyData("MoveSpeedAura", listOf(
                "EvocationRobe", "EvocationRobe_Unique1", "EvocationRobe_Unique2", "ArchersStrappings_Unique1", "SquidArmor", "SquidArmor_Unique1"
            )),
            ArmorPropertyData("MoveSpeedReduction", listOf()), // ??
            ArmorPropertyData("PetBat", listOf("SpelunkersArmor", "SpelunkersArmor_Unique1", "SpelunkersArmor_Year1")),
            ArmorPropertyData("PotionCooldownDecrease", listOf("ChampionsArmor", "ChampionsArmor_Unique1")),
            ArmorPropertyData("RangedDamageBoost", listOf(
                "ArchersStrappings", "ArchersStrappings_Unique1", "PhantomArmor", "PhantomArmor_Unique1"
            )),
            ArmorPropertyData("SoulGatheringBoost", listOf(
                "SoulRobe", "SoulRobe_Unique1", "GrimArmour", "GrimArmour_Unique1", "GrimArmour_Spooky2"
            )),
            ArmorPropertyData("SuperbDamageAbsorption", listOf(
                "GrimArmor_Unique1", "MercenaryArmor", "MercenaryArmor_Unique1", "OcelotArmor", "OcelotArmor_Unique1", "ReinforcedMail", "ReinforcedMail_Unique1",
                "ScaleMail", "ScaleMail_Unique1", "SnowArmor", "SnowArmor_Unique1", "TurtleArmor", "TurtleArmor_Unique1", "FullPlateArmor_Unique1", "FullPlateArmor",
                "ChampionsArmor", "ChampionsArmor_Unique1", "DarkArmor", "DarkArmor_Unique1", "BeenestArmor_Unique1", "MercenaryArmor_Spooky1", "FullPlateArmor_Spooky1",
                "GrimArmor_Spooky2", "MercenaryArmor_Spooky2"
            )),
            ArmorPropertyData("SlowResistance", listOf("ClimbingGear_Unique1", "SnowArmor", "SnowArmor_Unique1")),
            ArmorPropertyData("TeleportChance", listOf()), // Curious Armor, which is unused
            ArmorPropertyData("Beekeeper", listOf("BeenestArmor", "BeenestArmor_Unique1")),
            ArmorPropertyData("DodgeGhostForm", listOf("GhostArmor", "GhostArmor_Unique1", "GhostArmor_Spooky2")),
            ArmorPropertyData("EmeraldShield", listOf("EmeraldArmor_Unique1")),
            ArmorPropertyData("EnvironmentalProtection", listOf("ClimbingGear_Unique1")),
            ArmorPropertyData("Heavyweight", listOf("ClimbingGear", "ClimbingGear_Unique1", "ClimbingGear_Unique2")),
            ArmorPropertyData("DodgeRoot", listOf("SproutArmor", "SproutArmor_Unique1")),
            ArmorPropertyData("ItemCooldownReset", listOf("PiglinArmor", "PiglinArmor_Unique1", "NatureArmor", "NatureArmor_Unique1")),
            ArmorPropertyData("SquidRollLimited", listOf()), // ??
            ArmorPropertyData("SquidRollQuick", listOf("SquidArmor", "SquidArmor_Unique1")),
            ArmorPropertyData("ImmunityBoost", listOf("BardsGarb_Unique1")),
            ArmorPropertyData("InstantTransmission", listOf("EndRobes", "EndRobes_Unique1")),
            ArmorPropertyData("Resonant", listOf("BardsGarb_Unique1", "BardsGarb"))
    //        "FallResistance",
    //        "ReviveChance",
        )

        fun parsePak(index: PakIndex): Database {
            val levels = mutableSetOf<String>()
            val enchantments = mutableSetOf<EnchantmentData>()
            val items = mutableSetOf<ItemData>()

            index.forEach { pathString ->
                val _path = Path(pathString)
                val path = pathString.replaceAfterLast('.', "").removeSuffix(".")
                val pathLowercase = path.lowercase()
                val parentPath = "/Game".plus(_path.parent.pathString.removePrefix("/Dungeons/Content"))/*.replace(Constants.GameDataDirectoryPath, "")*/
                val parentName = _path.parent.name

                if (path.contains("ArmorProperties") && !path.contains("Cues")) {
                    return@forEach
                }

                if (path.contains("data") && path.contains("levels")) {
                    val levelKey = _path.name
                    levels.add(levelKey)
                    return@forEach
                }

                if (!_path.name.startsWith("T") || !path.lowercase().contains("_icon")) {
                    return@forEach
                }

                // remaining here is textures only

                if (path.contains("Enchantments") && pathLowercase.endsWith("_icon")) {
                    val enchantmentKey = correctKey(parentName)
                    val enchantment = EnchantmentData(
                        enchantmentKey,
                        parentPath,
                        PowerfulEnchantments.contains(enchantmentKey),
                        MultipleAllowedEnchantments.contains(enchantmentKey),
                        mutableSetOf<String>()
                            .apply {
                                if (ArmorEnchantments.contains(enchantmentKey)) this.add("Armor")
                                if (MeleeEnchantments.contains(enchantmentKey)) this.add("Melee")
                                if (RangedEnchantments.contains(enchantmentKey)) this.add("Ranged")
                                if (ExclusiveEnchantments.containsKey(enchantmentKey)) this.add("Exclusive")
                            }
                            .let { if (it.size == 0) null else it },
                        ExclusiveEnchantments[enchantmentKey],
                        EnchantmentSpecialDescValue[enchantmentKey]
                    )
                    if (enchantments.contains(enchantment)) return@forEach

                    enchantments.add(enchantment)
                }

                if (pathLowercase.endsWith("_icon_inventory")) {
                    if (itemsExcluded.any { parentName == it }) return@forEach

                    val itemKey = correctKey(parentName)
                    val type =
                        if (parentPath.contains("MeleeWeapons")) "Melee"
                        else if (parentPath.contains("RangedWeapons")) "Ranged"
                        else if (parentPath.contains("Armor")) "Armor"
                        else null

                    if (type != null) {
                        val gear = ItemData(itemKey, parentPath, type)
                        if (!items.contains(gear)) items.add(gear)
                    } else if (path.contains("Items")) {
                        val artifact = ItemData(itemKey, parentPath, "Artifact")
                        items.removeIf { artifact.type == it.type }
                        items.add(artifact)
                    }
                }

            }

            enchantments.add(EnchantmentData(id = "Unset", ""))

            return Database(armorProperties.toList(), enchantments.toList(), items.toList())
        }

        private fun correctKey(key: String): String {
            return keyMappings[key] ?: key
        }

    }

}
