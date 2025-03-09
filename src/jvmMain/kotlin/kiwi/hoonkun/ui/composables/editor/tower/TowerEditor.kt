package kiwi.hoonkun.ui.composables.editor.tower

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.LocalTextStyle
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
import androidx.compose.ui.zIndex
import kiwi.hoonkun.ArcticSettings
import kiwi.hoonkun.resources.Localizations
import kiwi.hoonkun.ui.composables.base.*
import kiwi.hoonkun.ui.composables.editor.collections.EmptyItemSlot
import kiwi.hoonkun.ui.composables.editor.collections.ItemHoverBorderModifier
import kiwi.hoonkun.ui.composables.editor.collections.ItemSlot
import kiwi.hoonkun.ui.composables.overlays.tower.TowerConfirmOverlay
import kiwi.hoonkun.ui.composables.overlays.tower.TowerItemModificationOverlay
import kiwi.hoonkun.ui.composables.overlays.tower.TowerModificationWarningOverlay
import kiwi.hoonkun.ui.composables.overlays.tower.TowerTileChallengeOverlay
import kiwi.hoonkun.ui.reusables.*
import kiwi.hoonkun.ui.states.EditorState
import kiwi.hoonkun.ui.states.LocalOverlayState
import kiwi.hoonkun.ui.states.Overlay
import kiwi.hoonkun.ui.units.dp
import kiwi.hoonkun.ui.units.sp
import minecraft.dungeons.resources.DungeonsLocalizations
import minecraft.dungeons.resources.DungeonsTextures
import minecraft.dungeons.states.MutableDungeons
import minecraft.dungeons.states.extensions.AnnotateTowerChallenge
import minecraft.dungeons.states.extensions.LocalizeTowerChallenge
import minecraft.dungeons.states.extensions.LocalizeTowerTile
import minecraft.dungeons.states.extensions.previewBitmap
import utils.padEnd

@Composable
fun BoxScope.TowerEditor(
    state: MutableDungeons.TowerMissionState?,
    hasInitialTower: Boolean,
    editor: EditorState
) {

    val overlays = LocalOverlayState.current

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

    val deleteTowerWithConfirm = {
        overlays.make {
            TowerConfirmOverlay(
                title = Localizations["tower_delete_all_title"],
                description = Localizations["tower_delete_all_description"],
                confirmLabel = Localizations["tower_delete_confirm"],
                requestClose = it,
                onConfirm = { editor.data.tower = null; it() }
            )
        }
    }

    MinimizableAnimatedContent(
        targetState = state,
        transitionSpec = {
            val enter = fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.975f, animationSpec = tween(220))
            val exit = fadeOut(animationSpec = tween(90))

            enter togetherWith exit
        },
        modifier = Modifier.fillMaxSize(),
    ) { capturedState ->

        if (capturedState != null) {

            LaunchedEffect(true) {
                if (editor.towerEditEnabled) return@LaunchedEffect

                overlays.make(canBeDismissed = false) {
                    val closeAndBack = { it(); editor.isInTowerEditMode = false }
                    val confirmAndEdit = { it(); editor.towerEditEnabled = true }

                    TowerModificationWarningOverlay(onConfirm = confirmAndEdit, requestClose = closeAndBack)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = scrollableFadeDistance, top = 32.dp),
                    modifier = Modifier
                        .weight(2.25f)
                        .then(scrollableFadeModifier)
                ) {
                    val items = capturedState.towerInfo.towerInfo.towerInfoFloors.zip(capturedState.towerInfo.towerConfig.floors)
                    itemsIndexed(items) { index, (info, config) ->
                        TowerFloorEditor(index, index == items.size - 1, info, config)
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .then(scrollableFadeModifier)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 48.dp, bottom = scrollableFadeDistance, start = 16.dp, end = 16.dp),
                ) {
                    Header(Localizations["tower_header_info"])

                    FieldsFlowRow {
                        IntegerValidatorField(
                            label = Localizations["tower_field_current_floor"],
                            value = capturedState.towerInfo.towerInfo.towerInfoCurrentFloor,
                            onChange = { capturedState.towerInfo.towerInfo.towerInfoCurrentFloor = it },
                            modifier = Modifier.weight(1f).zIndex(1f)
                        )
                        IntegerValidatorField(
                            label = Localizations["tower_field_lives_lost"],
                            value = capturedState.livesLost,
                            onChange = { capturedState.livesLost = it },
                            modifier = Modifier.weight(1f).zIndex(1f)
                        )
                        CheckField(
                            label = Localizations["tower_field_current_floor_completed"],
                            value = capturedState.towerInfo.towerCurrentFloorWasCompleted,
                            onChange = { capturedState.towerInfo.towerCurrentFloorWasCompleted = it },
                            modifier = Modifier.weight(1f)
                        )
                        IntegerValidatorField(
                            label = Localizations["tower_field_bosses_killed"],
                            value = capturedState.towerInfo.towerInfo.towerInfoBossesKilled,
                            onChange = { capturedState.towerInfo.towerInfo.towerInfoBossesKilled = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    RowRepeatedField(label = Localizations["tower_field_difficulty"], 1..3) {
                        TowerRetroButton(
                            selected = capturedState.missionDifficulty.difficulty == it,
                            onClick = { capturedState.missionDifficulty.difficulty = it },
                            content = { Text(DungeonsLocalizations["Difficulty/Difficulty_$it"]!!) },
                            modifier = RowScopedTowerRetroButtonModifier()
                        )
                    }

                    RowRepeatedField(label = Localizations["tower_field_threat_level"], 1..7) {
                        TowerRetroButton(
                            selected = capturedState.missionDifficulty.threatLevel == it,
                            onClick = { capturedState.missionDifficulty.threatLevel = it },
                            content = { Text(if (it == 1) "-" else "${it - 1}") },
                            modifier = RowScopedTowerRetroButtonModifier()
                        )
                    }

                    capturedState.towerInfo.towerPlayersData
                        .forEachIndexed { index, player -> TowerPlayerEditor(editor.data, index, player) }

                    Spacer(modifier = Modifier.height(32.dp))

                    TowerRetroButton(
                        onClick = deleteTowerWithConfirm,
                        color = { Color(0xffff6e25) },
                        content = { Text(Localizations["tower_delete_all_button"]) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(Localizations["tower_no_data"], modifier = Modifier.padding(bottom = 24.dp), fontSize = 30.sp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    val style = LocalTextStyle.current.copy(color = Color.White.copy(alpha = 0.5f), lineHeight = 30.sp)

                    val lines = List(3) { Localizations["tower_no_data_desc_${it}"] }

                    @Composable
                    fun Line(content: @Composable () -> Unit) = Row { Text("// ", style = style); content() }

                    lines.forEach { Line { Text(it, style = style) } }
                }
            }
        }

    }

    IncludeEditedTowerDataSwitcher(
        hasTower = state != null,
        currentValue = editor.data.includeEditedTower,
        onClick = { newValue -> editor.data.includeEditedTower = newValue },
    )

    ExperimentalFeatureWarning(
        hasNoTower = state == null,
        hasInitialTower = hasInitialTower,
        modifier = Modifier.align(Alignment.BottomStart)
    )
}


@Composable
private fun TowerFloorEditor(
    index: Int,
    lastFloor: Boolean,
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
        Header(
            if (index == 0) Localizations["tower_header_entrance_floor"]
            else Localizations["tower_header_floor", "$index"]
        )

        TowerFloorField(Localizations["tower_floor_field_type"]) {
            MutableDungeons.TowerMissionState.Info.InnerInfo.Floor.Type.entries.forEach {
                TowerRetroButton(
                    selected = it == info.towerFloorType,
                    onClick = { info.towerFloorType = it },
                    modifier = RowScopedTowerRetroButtonModifier(),
                    content = { Text(Localizations["tower_floor_type_${it.value}"]) },
                )
            }
        }

        TowerFloorField(Localizations["tower_floor_field_tile_and_challenge"]) {
            val onClick = { overlays.make(enter = fadeIn(), exit = fadeOut()) { TowerTileChallengeOverlay(config, config.tile, config.challenges, it) } }
            TowerRetroButton(
                onClick = onClick,
                modifier = Modifier.weight(1.1f),
                content = { Text(LocalizeTowerTile(config.tile)) },
            )
            TowerRetroButton(
                onClick = onClick,
                modifier = Modifier.weight(2f),
                content = {
                    Text(
                        config.challenges.getOrNull(0)
                            ?.let { AnnotateTowerChallenge(LocalizeTowerChallenge(it), null) }
                            ?: AnnotatedString("-")
                    )
                }
            )
        }

        TowerFloorField(Localizations["tower_floor_field_rewards"]) {
            config.rewards.forEachIndexed { rewardIndex, configReward ->
                val rewardBitmap = remember(configReward) {
                    try { configReward.previewBitmap() }
                    catch(e: Exception) { null }
                }
                TowerRetroButton(
                    onClick = { config.rewards[rewardIndex] = configReward.next() },
                    modifier = Modifier.weight(1f).aspectRatio(1f / 1f),
                    content = { if (rewardBitmap != null) Image(bitmap = rewardBitmap, contentDescription = null)  }
                )
            }

            val enchantmentPointBitmap = remember {
                DungeonsTextures["/UI/Materials/Inventory2/Enchantment/enchant_counter.png"]
            }
            TowerRetroButton(
                modifier = Modifier.weight(1f).aspectRatio(1f / 1f).alpha(0.4f),
                enabled = !lastFloor,
                content = { if (!lastFloor) Image(bitmap = enchantmentPointBitmap, contentDescription = null)  }
            )
        }
    }
}


@Composable
private fun TowerPlayerEditor(dungeons: MutableDungeons, index: Int, player: MutableDungeons.TowerMissionState.Info.PlayerData) {

    val overlays = LocalOverlayState.current

    val equipments = player.playerItems.slice(indices = 0..<3)
    val artifacts = player.playerItems.padEnd(minSize = 6, factory = { null }).slice(3..<6)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Header(Localizations["tower_header_player", "${index + 1}"])

        var selected by remember { mutableStateOf(-1) }

        val makeItemOverlay = { index: Int ->
            selected = index
            val item = if (index in 0..<3) equipments[index] else artifacts[index - 3]

            overlays.make(
                backdropOptions = Overlay.BackdropOptions(
                    blur = false,
                    alpha = 0f,
                    onClick = { selected = -1; destroy(it.id) }
                ),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TowerItemModificationOverlay(
                    dungeons = dungeons,
                    index = index,
                    oldItem = item,
                    onUpdate = { newItem ->
                        if (player.playerItems.size <= index) {
                            player.playerItems.add(newItem)
                        } else {
                            player.playerItems[index] = newItem
                        }
                    },
                    requestClose = { selected = -1; it() }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            equipments.forEachIndexed { index, item ->
                ItemSlot(item, modifier = ItemModifier(selected == index) { makeItemOverlay(index) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            artifacts.forEachIndexed { index, item ->
                val offsetIndex = index + 3
                val modifier = ItemModifier(selected == offsetIndex) { makeItemOverlay(offsetIndex) }
                if (item == null) EmptyItemSlot(modifier = modifier)
                else ItemSlot(item, modifier = modifier)
            }
        }

        FieldsFlowRow {
            IntegerValidatorField(
                label = Localizations["tower_player_field_floor"],
                value = player.playerLastFloorIndex,
                onChange = { player.playerLastFloorIndex = it },
                modifier = Modifier.weight(1f)
            )
            IntegerValidatorField(
                label = Localizations["tower_player_field_arrows"],
                value = player.playerArrowsAmmount,
                onChange = { player.playerArrowsAmmount = it },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
            IntegerValidatorField(
                label = Localizations["tower_player_field_points"],
                value = player.playerEnchantmentPointsGranted,
                onChange = { player.playerEnchantmentPointsGranted = it },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RowScope.ItemModifier(selected: Boolean, onClick: () -> Unit): Modifier {
    val interaction = rememberMutableInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()

    return Modifier
        .aspectRatio(1f / 1f)
        .weight(1f)
        .hoverable(interaction)
        .clickable(interaction, null) { onClick() }
        .then(ItemHoverBorderModifier(selected = selected, hovered = hovered))
}


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
    onClick: () -> Unit = { },
    selected: Boolean = false,
    enabled: Boolean = true,
    color: () -> Color = { if (selected) Color(0xffa85632) else Color(0xff444444) },
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    RetroButton(
        color = color,
        hoverInteraction = RetroButtonHoverInteraction.Outline,
        stroke = 3.dp,
        radius = RetroButtonDpCornerRadius(all = 4.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        enabled = enabled,
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
    hasTower: Boolean,
    currentValue: Boolean,
    onClick: (Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val slideDistance = with(density) { 15.dp.roundToPx() }

    MinimizableAnimatedContent(
        targetState = currentValue to hasTower,
        transitionSpec = minimizableContentTransform {
            val enter = defaultFadeIn() + slideInHorizontally { slideDistance }
            val exit = defaultFadeOut() + slideOutHorizontally { slideDistance }

            enter togetherWith exit using SizeTransform(false)
        },
        modifier = Modifier.fillMaxSize().padding(end = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) { (capturedCurrent, capturedHasTower) ->
        if (capturedHasTower) {
            val annotation = LinkAnnotation.Clickable(
                tag = "include_edited_tower",
                styles = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline, color = Color(0xffff884c))),
                linkInteractionListener = { onClick(!capturedCurrent) }
            )
            val textFirstLine = buildAnnotatedString {
                val a = { withLink(annotation) { append(Localizations["tower_use_edited_data_objective_${capturedCurrent}"]) } }
                val d = { append(" ") }
                val b = { append(Localizations["tower_use_edited_data"]) }

                if (ArcticSettings.locale == "en") {
                    a(); d(); b()
                } else {
                    b(); d(); a()
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(text = textFirstLine)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = Localizations["tower_use_edited_data_description_${capturedCurrent}"], color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun ExperimentalFeatureWarning(
    hasNoTower: Boolean,
    hasInitialTower: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val slideDistance = with(density) { -15.dp.roundToPx() }

    MinimizableAnimatedContent(
        targetState = hasNoTower to hasInitialTower,
        transitionSpec = minimizableContentTransform {
            val enter = defaultFadeIn() + slideInHorizontally { slideDistance }
            val exit = defaultFadeOut() + slideOutHorizontally { slideDistance }

            enter togetherWith exit using SizeTransform(false)
        },
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) { (hasNoTower, hasInitialTower) ->
        if (!hasNoTower) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom,
                modifier = modifier
            ) {
                Text(Localizations["tower_experimental_warning_desc_1_${hasInitialTower}"])
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    Localizations["tower_experimental_warning_desc_2_${hasInitialTower}"],
                    color = Color(0xffff884c)
                )
            }
        }
    }
}
