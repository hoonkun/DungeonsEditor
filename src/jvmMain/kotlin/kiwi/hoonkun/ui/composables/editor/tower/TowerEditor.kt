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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.composables.editor.collections.EmptyItemSlot
import kiwi.hoonkun.ui.composables.editor.collections.ItemSlot
import kiwi.hoonkun.ui.composables.overlays.tower.TowerTileChallengeOverlay
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.units.dp
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.LocalizeTowerTile
import minecraft.dungeons.states.extensions.previewBitmap
import utils.padEnd

@Composable
fun BoxScope.TowerEditor(
    state: MutableDungeons.TowerMissionState,
    hasInitialTower: Boolean,
    editor: EditorState
) {

    val scrollableFadeDistance = 128.dp
    val scrollableFadeModifier = Modifier
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color(0x00000000),
                    1f to Color(0xff000000),
                    startY = size.height - scrollableFadeDistance.toPx(),
                ),
                blendMode = BlendMode.DstOut
            )
        }

    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = scrollableFadeDistance, top = 32.dp),
            modifier = Modifier
                .weight(2.25f)
                .then(scrollableFadeModifier)
        ) {
            itemsIndexed(state.towerInfo.towerInfo.towerInfoFloors.zip(state.towerInfo.towerConfig.floors)) { index, (info, config) ->
                TowerFloorEditor(index, info, config)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .then(scrollableFadeModifier)
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp, bottom = scrollableFadeDistance),
        ) {
            Header("타워 기본 정보")

            FieldsFlowRow {
                IntegerValidatorField(
                    label = "현재 진행중인 층수",
                    value = state.towerInfo.towerInfo.towerInfoCurrentFloor,
                    onChange = { state.towerInfo.towerInfo.towerInfoCurrentFloor = it },
                    modifier = Modifier.weight(1f).zIndex(1f)
                )
                IntegerValidatorField(
                    label = "진행 중 쓰러진 횟수",
                    value = state.livesLost,
                    onChange = { state.livesLost = it },
                    modifier = Modifier.weight(1f).zIndex(1f)
                )
                CheckField(
                    label = "현재 층의 완료 여부",
                    value = state.towerInfo.towerCurrentFloorWasCompleted,
                    onChange = { state.towerInfo.towerCurrentFloorWasCompleted = it },
                    modifier = Modifier.weight(1f)
                )
                IntegerValidatorField(
                    label = "진행 중 쓰러뜨린 보스의 수",
                    value = state.towerInfo.towerInfo.towerInfoBossesKilled,
                    onChange = { state.towerInfo.towerInfo.towerInfoBossesKilled = it },
                    modifier = Modifier.weight(1f)
                )
            }

            RowRepeatedField(label = "설정 난이도", 1..3) {
                TowerRetroButton(
                    selected = state.missionDifficulty.difficulty == it,
                    onClick = { state.missionDifficulty.difficulty = it },
                    content = { Text(DungeonsLocalizations["Difficulty/Difficulty_$it"]!!) },
                    modifier = RowScopedTowerRetroButtonModifier()
                )
            }

            RowRepeatedField(label = "위협 레벨", 1..7) {
                TowerRetroButton(
                    selected = state.missionDifficulty.threatLevel == it,
                    onClick = { state.missionDifficulty.threatLevel = it },
                    content = { Text(if (it == 1) "-" else "${it - 1}") },
                    modifier = RowScopedTowerRetroButtonModifier()
                )
            }

            state.towerInfo.towerPlayersData
                .forEachIndexed { index, player -> TowerPlayerEditor(index, player) }
        }
    }

    IncludeEditedTowerDataSwitcher(
        currentValue = editor.data.includeEditedTower,
        onClick = { newValue -> editor.data.includeEditedTower = newValue },
    )

    ExperimentalFeatureWarning(
        hasInitialTower = hasInitialTower,
        modifier = Modifier.align(Alignment.BottomStart)
    )
}


@Composable
private fun TowerFloorEditor(
    index: Int,
    info: MutableDungeons.TowerMissionState.Info.InnerInfo.Floor,
    config: MutableDungeons.TowerMissionState.Info.Config.Floor
) {

    val overlays = LocalOverlayState.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .drawWithCache {
                val path = RetroIndicator()
                onDrawBehind { drawPath(path, color = Color(0xff272727)) }
            }
            .padding(16.dp)
    ) {
        Header(if (index == 0) "입구 층" else "$index 층")

        TowerFloorField("층 유형") {
            MutableDungeons.TowerMissionState.Info.InnerInfo.Floor.Type.entries.forEach {
                TowerRetroButton(
                    selected = it == info.towerFloorType,
                    onClick = { info.towerFloorType = it },
                    modifier = RowScopedTowerRetroButtonModifier(),
                    content = { Text(Localizations["tower_floor_type_${it.value}"]) },
                )
            }
        }

        TowerFloorField("지형 및 도전") {
            TowerRetroButton(
                onClick = { overlays.make { TowerTileChallengeOverlay(config, config.tile, config.challenges, it) } },
                modifier = Modifier.weight(1.1f),
                content = { Text(LocalizeTowerTile(config.tile)) },
            )
            TowerRetroButton(
                onClick = { overlays.make { TowerTileChallengeOverlay(config, config.tile, config.challenges, it) } },
                modifier = Modifier.weight(2f),
                content = { Text(config.challenges.getOrNull(0) ?: "-") }
            )
        }

        TowerFloorField("보상 유형") {
            config.rewards.forEach { configReward ->
                val rewardBitmap = remember(configReward) {
                    try { configReward.previewBitmap() }
                    catch(e: Exception) { null }
                }
                TowerRetroButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).aspectRatio(1f / 1f),
                    content = { if (rewardBitmap != null) Image(bitmap = rewardBitmap, contentDescription = null)  }
                )
            }

            val enchantmentPointBitmap = remember {
                DungeonsTextures["/UI/Materials/Inventory2/Enchantment/enchant_counter.png"]
            }
            TowerRetroButton(
                onClick = { },
                modifier = Modifier.weight(1f).aspectRatio(1f / 1f).alpha(0.4f),
                content = { if (index != 30) Image(bitmap = enchantmentPointBitmap, contentDescription = null)  }
            )
        }
    }
}


@Composable
private fun TowerPlayerEditor(index: Int, player: MutableDungeons.TowerMissionState.Info.PlayerData) {

    val equipments = player.playerItems.slice(indices = 0..<3)
    val artifacts = player.playerItems.padEnd(minSize = 6, factory = { null }).slice(3..<6)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Header("${index + 1} 번째 플레이어")

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            equipments.forEach {
                ItemSlot(it, modifier = ItemModifier())
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            artifacts.forEach {
                if (it == null) EmptyItemSlot(modifier = ItemModifier())
                else ItemSlot(it, modifier = ItemModifier())
            }
        }

        FieldsFlowRow {
            IntegerValidatorField(
                label = "플레이어의 현재 층수",
                value = player.playerLastFloorIndex,
                onChange = { player.playerLastFloorIndex = it },
                modifier = Modifier.weight(1f)
            )
            IntegerValidatorField(
                label = "보유 화살 수",
                value = player.playerArrowsAmmount,
                onChange = { player.playerArrowsAmmount = it },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
            IntegerValidatorField(
                label = "보유 효과부여 포인트",
                value = player.playerEnchantmentPointsGranted,
                onChange = { player.playerEnchantmentPointsGranted = it },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Stable
private fun RowScope.ItemModifier() = Modifier.aspectRatio(1f / 1f).weight(1f)


@Composable
private fun Header(text: String) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .drawWithCache {
                val path = RetroIndicator(dpRadius = RetroButtonDpCornerRadius(all = 4.dp))
                onDrawBehind { drawPath(path, color = Color(0xff3f8e4f)) }
            }
            .padding(vertical = 16.dp),
    ) {
        Text(text)
    }


@Composable
private fun FieldsFlowRow(
    maxItemsInEachRow: Int = 2,
    content: @Composable FlowRowScope.() -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.zIndex(1f),
        maxItemsInEachRow = maxItemsInEachRow,
        content = content
    )
}


@Composable
private fun Field(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) =
    Column(modifier = modifier) {
        Text(text = label)
        content()
    }

@Composable
private fun RowRepeatedField(
    label: String,
    indices: IntRange,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.(Int) -> Unit
) =
    Field(label = label, modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            indices.forEach { content(it) }
        }
    }

@Composable
private fun IntegerValidatorField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) =
    Field(label = label, modifier = modifier) {
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
private fun CheckField(
    label: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) =
    Field(label = label, modifier = modifier) {
        Image(
            bitmap = remember { DungeonsTextures["/UI/Materials/Chests/unlocked_checkmark.png"] },
            contentDescription = null,
            modifier = Modifier
                .padding(top = 4.dp)
                .width(32.dp)
                .grayscale { if (value) 1f else 0f }
                .clickable(
                    interactionSource = rememberMutableInteractionSource(),
                    indication = null,
                    onClick = { onChange(!value) }
                )
        )
    }

@Composable
private fun TowerFloorField(
    label: String,
    content: @Composable RowScope.() -> Unit
) =
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, modifier = Modifier.width(125.dp).padding(start = 8.dp))
        content()
    }


@Composable
private fun TowerRetroButton(
    onClick: () -> Unit,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    RetroButton(
        color = { if (selected) Color(0xffa85632) else Color(0xff444444) },
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        stroke = 3.dp,
        radius = RetroButtonDpCornerRadius(all = 4.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = modifier,
        onClick = onClick,
        content = content
    )
}

@Stable
private fun RowScope.RowScopedTowerRetroButtonModifier() =
    Modifier.size(Dp.Unspecified).weight(1f)


@Composable
private fun IncludeEditedTowerDataSwitcher(
    currentValue: Boolean,
    onClick: (Boolean) -> Unit,
) {
    val density = LocalDensity.current

    MinimizableAnimatedContent(
        targetState = currentValue,
        transitionSpec = minimizableContentTransform {
            val enter = defaultFadeIn() + slideIn { IntOffset(with(density) { 15.dp.roundToPx() }, 0) }
            val exit = defaultFadeOut() + slideOut { IntOffset(with(density) { 15.dp.roundToPx() }, 0) }

            enter togetherWith exit using SizeTransform(false)
        },
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) { capturedCurrent ->

        val annotation = LinkAnnotation.Clickable(
            tag = "include_edited_tower",
            styles = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = Color(0xffff884c))),
            linkInteractionListener = { onClick(!capturedCurrent) }
        )
        val textFirstLine = buildAnnotatedString {
            append("수정한 탑 데이터를 ")
            withLink(annotation) { append(if (capturedCurrent) "사용합니다" else "사용하지 않습니다.") }
        }

        val textSecondLine =
            if (capturedCurrent) "현재 보이는 데이터를 저장 시 반영합니다."
            else "수정한 데이터를 반영하지 않고 기존 데이터를 유지합니다."

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(text = textFirstLine)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = textSecondLine, color = Color.White.copy(alpha = 0.6f))
        }

    }
}

@Composable
private fun ExperimentalFeatureWarning(
    hasInitialTower: Boolean,
    modifier: Modifier = Modifier
) =
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
