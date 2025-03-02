package kiwi.hoonkun.ui.composables.editor.tower

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.composables.editor.collections.EmptyItemSlot
import kiwi.hoonkun.ui.composables.editor.collections.ItemSlot
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.resources.DungeonsTower
import minecraft.dungeons.states.MutableDungeons
import utils.padEnd

@Composable
fun TowerEditor(
    state: MutableDungeons.TowerMissionState,
    hasInitialTower: Boolean,
    editor: EditorState
) {

    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.weight(2.25f)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 128.dp, top = 32.dp),
                    modifier = Modifier
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to Color(0x00000000),
                                    1f to Color(0xff000000),
                                    startY = size.height - 128.dp.toPx(),
                                ),
                                blendMode = BlendMode.DstOut
                            )
                        }
                ) {
                    itemsIndexed(state.towerInfo.towerInfo.towerInfoFloors.zip(state.towerInfo.towerConfig.floors)) { index, (info, config) ->
                        TowerFloorEditor(index, info, config)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                0f to Color(0x00000000),
                                1f to Color(0xff000000),
                                startY = size.height - 128.dp.toPx(),
                            ),
                            blendMode = BlendMode.DstOut
                        )
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(top = 48.dp, bottom = 128.dp),
            ) {
                Index("타워 기본 정보")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 16.dp).zIndex(1f)
                ) {
                    Column(
                        modifier = Modifier.weight(1f).zIndex(2f)
                    ) {
                        IntegerValidatorField(
                            label = "현재 진행중인 층수",
                            value = state.towerInfo.towerInfo.towerInfoCurrentFloor,
                            onChange = { state.towerInfo.towerInfo.towerInfoCurrentFloor = it }
                        )
                        Text("현재 층의 완료 여부", modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
                        Image(
                            bitmap = remember { DungeonsTextures["/UI/Materials/Chests/unlocked_checkmark.png"] },
                            contentDescription = null,
                            modifier = Modifier.width(32.dp)
                                .grayscale { if (state.towerInfo.towerCurrentFloorWasCompleted) 1f else 0f }
                                .clickable(
                                    interactionSource = rememberMutableInteractionSource(),
                                    indication = null
                                ) { state.towerInfo.towerCurrentFloorWasCompleted = !state.towerInfo.towerCurrentFloorWasCompleted }
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f).zIndex(1f)
                    ) {
                        IntegerValidatorField(
                            label = "진행 중 쓰러진 횟수",
                            value = state.livesLost,
                            onChange = { state.livesLost = it }
                        )
                        IntegerValidatorField(
                            label = "진행 중 쓰러뜨린 보스의 수",
                            value = state.towerInfo.towerInfo.towerInfoBossesKilled,
                            onChange = { state.towerInfo.towerInfo.towerInfoBossesKilled = it }
                        )
                    }
                }
                Text("설정 난이도", modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..3).forEach {
                        DifficultyToggle(
                            text = DungeonsLocalizations["Difficulty/Difficulty_$it"]!!,
                            selected = state.missionDifficulty.difficulty == it,
                            difficulty = it,
                            onClick = { state.missionDifficulty.difficulty = it }
                        )
                    }
                }

                Text("위협 레벨", modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..8).forEach {
                        DifficultyToggle(
                            text = "$it",
                            selected = state.missionDifficulty.threatLevel == it,
                            difficulty = it,
                            onClick = { state.missionDifficulty.threatLevel = it }
                        )
                    }
                }

                state.towerInfo.towerPlayersData.forEachIndexed { index, player ->
                    TowerPlayerEditor(index, player)
                }
            }
        }

        MinimizableAnimatedContent(
            targetState = editor.data.includeEditedTower,
            transitionSpec = minimizableContentTransform {
                val enter = defaultFadeIn() + slideIn { IntOffset(with(density) { 15.dp.roundToPx() }, 0) }
                val exit = defaultFadeOut() + slideOut { IntOffset(with(density) { 15.dp.roundToPx() }, 0) }

                enter togetherWith exit using SizeTransform(false)
            },
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            IncludeEditedTowerDataSwitcher(
                currentValue = it,
                onClick = { newValue -> editor.data.includeEditedTower = newValue },
            )
        }

        ExperimentalFeatureWarning(
            hasInitialTower = hasInitialTower,
            modifier = Modifier.align(Alignment.BottomStart)
        )

    }

}

@Composable
private fun TowerPlayerEditor(index: Int, player: MutableDungeons.TowerMissionState.Info.PlayerData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 36.dp)
    ) {
        Index("${index + 1} 번째 플레이어")

        val equipments = player.playerItems.slice(0..<3)
        val artifacts = player.playerItems.padEnd(6) { null }.slice(3..<6)

        val modifier = Modifier.aspectRatio(1f / 1f)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            equipments.forEach {
                ItemSlot(it, modifier = modifier.weight(1f))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            artifacts.forEach {
                if (it == null) EmptyItemSlot(modifier = modifier.weight(1f))
                else ItemSlot(it, modifier = modifier.weight(1f))
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                IntegerValidatorField(
                    label = "플레이어의 현재 층수",
                    value = player.playerLastFloorIndex,
                    onChange = { player.playerLastFloorIndex = it },
                    modifier = Modifier
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                IntegerValidatorField(
                    label = "보유 화살 수",
                    value = player.playerArrowsAmmount,
                    onChange = { player.playerArrowsAmmount = it },
                    modifier = Modifier
                )
                IntegerValidatorField(
                    label = "보유 효과부여 포인트",
                    value = player.playerEnchantmentPointsGranted,
                    onChange = { player.playerEnchantmentPointsGranted = it },
                )
            }
        }
    }
}

@Composable
private fun RowScope.DifficultyToggle(
    text: String,
    selected: Boolean,
    difficulty: Int,
    onClick: (Int) -> Unit
) {
    RetroButton(
        text = text,
        color = if (selected) Color(0xffa85632) else Color(0xff444444),
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        stroke = 3.dp,
        radius = RetroButtonDpCornerRadius(all = 4.dp),
        onClick = { onClick(difficulty) },
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = Modifier.size(Dp.Unspecified).weight(1f),
    )
}

@Composable
private fun IntegerValidatorField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier.padding(top = 24.dp)
) {
    Text(label, modifier = modifier)

    var mutableValue by remember(value) { mutableStateOf("$value") }
    TextFieldValidatable(
        value = mutableValue,
        onValueChange = { mutableValue = it },
        validator = { it.toIntOrNull() != null },
        onSubmit = { onChange(it.toInt()) },
        direction = PopupDirection.Bottom
    )
}

@Composable
private fun TowerFloorEditor(
    index: Int,
    info: MutableDungeons.TowerMissionState.Info.InnerInfo.Floor,
    config: MutableDungeons.TowerMissionState.Info.Config.Floor
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .drawWithCache {
                val path = RetroIndicator()
                onDrawBehind { drawPath(path, color = Color(0xff272727)) }
            }
            .padding(16.dp)
    ) {
        Index(if (index == 0) "입장 층" else "$index 층")

        TowerFloorField("층 유형") {
            MutableDungeons.TowerMissionState.Info.InnerInfo.Floor.Type.entries.forEach {
                FloorTypeToggle(
                    selected = it == info.towerFloorType,
                    thisType = it,
                    onClick = { info.towerFloorType = it }
                )
            }
        }

        TowerFloorField("지형 및 도전") {
            TowerTileChallengeToggle(
                value = LocalizeTowerTile(config.tile),
                entries = DungeonsTower.tiles,
                modifier = Modifier.weight(1.1f)
            ) { config.tile = it }

            TowerTileChallengeToggle(
                value = config.challenges.getOrNull(0),
                entries = DungeonsTower.challenges,
                modifier = Modifier.weight(2f)
            ) { config.challenges[0] = it }
        }

        TowerFloorField("보상 유형") {
            config.rewards.forEachIndexed { configIndex, configReward ->
                FloorRewardToggle(thisType = configReward) { config.rewards[configIndex] = it }
            }
            FloorRewardToggle(thisType = null)
        }
    }
}

@Stable
private fun LocalizeTowerTile(tile: String): String {
    if (tile.startsWith("twr_floor_")) {
        var result = tile.removePrefix("twr_floor_")
        DungeonsTower.AreaLocalizations.forEach {
            result = result.replace(it, Localizations["tower_tile_label_$it"].trim())
        }
        return result.removePrefix("_g").replace("_", " ")
    } else {
        var result = tile.removePrefix("twr_")
        DungeonsTower.AreaLocalizations.forEach {
            result = result.replace(
                it,
                Localizations["tower_tile_label_$it"].trim()
            )
        }
        DungeonsTower.AreaDungeonsLocalizations.forEach { (shortName, localizationName) ->
            result = result.replace(
                shortName,
                DungeonsLocalizations["Mission/${localizationName}_name"]!!.trim().plus(" ")
            )
        }
        return result
            .replace("  ", " ")
            .removePrefix("_g")
            .replace("_floor_", "")
            .replace("_", " ")
    }
}

@Composable
private fun Index(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .drawWithCache {
                val path = RetroIndicator(dpRadius = RetroButtonDpCornerRadius(all = 4.dp))
                onDrawBehind { drawPath(path, color = Color(0xff3f8e4f)) }
            }
            .padding(vertical = 16.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun TowerFloorField(label: String, content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, modifier = Modifier.width(125.dp).padding(start = 8.dp))
        content()
    }
}

@Composable
private fun RowScope.FloorTypeToggle(
    selected: Boolean,
    thisType: MutableDungeons.TowerMissionState.Info.InnerInfo.Floor.Type,
    onClick: (MutableDungeons.TowerMissionState.Info.InnerInfo.Floor.Type) -> Unit
) {
    RetroButton(
        text = Localizations["tower_floor_type_${thisType.value}"],
        color = if (selected) Color(0xffa85632) else Color(0xff444444),
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        stroke = 3.dp,
        radius = RetroButtonDpCornerRadius(all = 4.dp),
        onClick = { onClick(thisType) },
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = Modifier.size(Dp.Unspecified).weight(1f),
    )
}

@Composable
private fun RowScope.FloorRewardToggle(
    thisType: MutableDungeons.TowerMissionState.Info.Config.Floor.Reward?,
    onClick: (MutableDungeons.TowerMissionState.Info.Config.Floor.Reward) -> Unit = { }
) {
    val bitmap = remember(thisType) {
        when(thisType) {
            MutableDungeons.TowerMissionState.Info.Config.Floor.Reward.Any ->
                DungeonsTextures["/Actors/Items/MysteryBoxes/MysteryBoxAny/T_MysteryBoxAny_Icon.png"]
            MutableDungeons.TowerMissionState.Info.Config.Floor.Reward.Melee ->
                DungeonsTextures["/Actors/Items/MysteryBoxes/MysteryBoxMelee/T_MysteryBoxMelee_Icon.png"]
            MutableDungeons.TowerMissionState.Info.Config.Floor.Reward.Armor ->
                DungeonsTextures["/Actors/Items/MysteryBoxes/MysteryBoxArmor/T_MysteryBoxArmor_Icon.png"]
            MutableDungeons.TowerMissionState.Info.Config.Floor.Reward.Ranged ->
                DungeonsTextures["/Actors/Items/MysteryBoxes/MysteryBoxRanged/T_MysteryBoxRanged_Icon.png"]
            MutableDungeons.TowerMissionState.Info.Config.Floor.Reward.Artifact ->
                DungeonsTextures["/Actors/Items/MysteryBoxes/MysteryBoxArtifact/T_MysteryBoxArtifact_Icon.png"]
            null ->
                DungeonsTextures["/UI/Materials/Inventory2/Enchantment/enchant_counter.png"]
        }
    }

    RetroButton(
        color = { Color(0xff444444) },
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        onClick = { if (thisType != null) onClick(thisType) },
        contentPadding = PaddingValues(all = 16.dp),
        stroke = 3.dp,
        radius = RetroButtonDpCornerRadius(all = 4.dp),
        enabled = thisType != null,
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f / 1f)
            .alpha(if (thisType == null) 0.4f else 1f)
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
        )
    }

}

@Composable
private fun TowerTileChallengeToggle(
    value: String?,
    entries: List<String>,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    RetroButton(
        text = value ?: "-",
        color = { Color(0xff444444) },
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        onClick = { },
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 0.dp),
        stroke = 3.dp,
        radius = RetroButtonDpCornerRadius(all = 4.dp),
        textOverflow = TextOverflow.MiddleEllipsis,
        textPadding = PaddingValues(all = 0.dp),
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
private fun IncludeEditedTowerDataSwitcher(
    currentValue: Boolean,
    onClick: (Boolean) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            buildAnnotatedString {
                append("수정한 탑 데이터를 ")

                withLink(
                    LinkAnnotation.Clickable(
                        tag = "include_edited_tower",
                        styles = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = Color(0xffff884c))),
                    ) {
                        onClick(!currentValue)
                    }
                ) {
                    append(
                        if (currentValue) "사용합니다"
                        else "사용하지 않습니다."
                    )
                }
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (currentValue) "현재 보이는 데이터를 저장 시 반영합니다."
            else "수정한 데이터를 반영하지 않고 기존 데이터를 유지합니다.",
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ExperimentalFeatureWarning(
    hasInitialTower: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        Text(
            if (hasInitialTower) "현재 기존 탑 데이터를 수정하고 있습니다."
            else "현재 새 탑 데이터를 추가하고 있습니다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (hasInitialTower) "실험적 기능입니다. 일부 기존 데이터가 유실되거나 게임이 제대로 동작하지 않을 수 있습니다."
            else "실험적 기능입니다. 로드 시 게임이 제대로 동작하지 않을 수 있습니다.",
            color = Color(0xffff884c)
        )
    }
}
