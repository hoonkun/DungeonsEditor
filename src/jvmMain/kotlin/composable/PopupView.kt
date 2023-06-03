package composable

import dungeons.ItemData
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import arctic.states.ArmorProperty
import arctic.states.Enchantment
import arctic.states.Item
import arctic.ui.unit.dp
import arctic.ui.unit.sp
import blackstone.states.*
import arctic.states.extensions.addItem
import arctic.states.extensions.data
import arctic.states.extensions.deleteItem
import arctic.states.extensions.where
import composable.blackstone.popup.*

@Composable
fun BoxScope.Popups() {
    Debugging.recomposition("Popups")

    InventoryFullPopup()
    SavedPopup()

    EnchantmentPopup()
    ArmorPropertyPopup()

    ItemCreationPopup()
    ItemEditionPopup()

    DeletionPopup()
    DuplicatePopup()

    InputFileProcessFailedPopup()
    SaveInProgressPopup()
    CloseFilePopup()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CloseFilePopup() {
    val _enabled = arctic.alerts.closeFile

    Backdrop(_enabled) { arctic.alerts.closeFile = false }

    AnimatedContent(
        targetState = _enabled,
        transitionSpec = {
            val enter = fadeIn() + scaleIn(initialScale = 1.1f)
            val exit = fadeOut() + scaleOut(targetScale = 1.1f)
            enter with exit
        },
        modifier = Modifier.fillMaxSize()
    ) { enabled ->
        if (enabled) {
            val message = "정말 편집을 마치고 파일을 닫으시겠어요?"
            val description = "마지막 저장 이후에 만든 변경사항은 저장되지 않아요"

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(80.dp))
                Row {
                    RetroButton("취소", Color(0xffffffff), "overlay") { arctic.alerts.closeFile = false }
                    Spacer(modifier = Modifier.width(75.dp))
                    RetroButton("닫기", Color(0xffff6e25), "outline") {
                        arctic.selection.clearSelection()
                        arctic.stored = null
                        arctic.alerts.closeFile = false
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InputFileProcessFailedPopup() {
    val _enabled: String? = arctic.alerts.fileLoadFailed

    Backdrop(_enabled != null, 0.6f) {  }

    AnimatedContent(
        targetState = _enabled,
        transitionSpec = {
            val enter = fadeIn() + scaleIn(initialScale = 1.1f)
            val exit = fadeOut() + scaleOut(targetScale = 1.1f)
            enter with exit
        },
    ) { enabled ->
        if (enabled != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "파일 로드에 실패했어요",
                    color = Color.White,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "잘못된 파일이거나, 개발자가 이상한 짓을 해서 그럴 수도 있어요.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "$enabled",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(50.dp))
                RetroButton("닫기", Color(0xffffffff), "overlay") { arctic.alerts.fileLoadFailed = null }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SaveInProgressPopup() {
    val _enabled: Boolean = arctic.dialogs.fileSaveDstSelector

    Backdrop(_enabled, 0.6f) {  }

    AnimatedVisibility(
        visible = _enabled,
        enter = fadeIn() + scaleIn(initialScale = 1.1f),
        exit = fadeOut() + scaleOut(targetScale = 1.1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "저장 진행 중!",
                color = Color.White,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "열린 파일 탐색기에서 저장할 위치를 선택해주세요",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun InventoryFullPopup() {
    val _enabled: Boolean = arctic.alerts.inventoryFull

    Backdrop(_enabled) { arctic.alerts.inventoryFull = false }

    AnimatedVisibility(
        visible = _enabled,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "인벤토리가 가득 차서 더 이상 추가할 수 없어요. 먼저 아이템을 삭제하거나 창고로 옮겨보세요!",
                color = Color.White,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "닫으려면 아무 곳이나 누르세요",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 24.sp
            )
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DuplicatePopup() {

    val _target: Item? = arctic.duplication.target

    Backdrop(_target != null) { arctic.duplication.target = null }

    AnimatedContent(
        targetState = _target to _target?.where,
        transitionSpec = {
            val enter = fadeIn() + scaleIn(initialScale = 1.1f)
            val exit = fadeOut() + scaleOut(targetScale = 1.1f)
            enter with exit
        },
        modifier = Modifier.fillMaxSize()
    ) { (target, where) ->
        if (target != null) {
            val message = "${if (where == "inventory") "인벤토리" else "창고"}에 있는 아이템이에요. 어디에 복제하시겠어요?"
            val description = "지금은 ${if (arctic.view == "inventory") "인벤토리" else "창고"}를 보고있어요."

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(80.dp))
                Row {
                    RetroButton("원래 위치에 복제", Color(0xff3f8e4f), "outline") { target.parent.addItem(target.copy(), target); arctic.duplication.target = null }
                    Spacer(modifier = Modifier.width(75.dp))
                    RetroButton("취소", Color(0xffffffff), "overlay") { arctic.duplication.target = null }
                    Spacer(modifier = Modifier.width(75.dp))
                    RetroButton("여기에 복제", Color(0xff3f8e4f), "outline") { target.parent.addItem(target.copy()); arctic.duplication.target = null }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DeletionPopup() {

    val _target: Item? = arctic.deletion.target

    Backdrop(_target != null) { arctic.deletion.target = null }

    AnimatedContent(
        targetState = _target to _target?.where,
        transitionSpec = {
            val enter = fadeIn() + scaleIn(initialScale = 1.1f)
            val exit = fadeOut() + scaleOut(targetScale = 1.1f)
            enter with exit
        },
        modifier = Modifier.fillMaxSize()
    ) { (target, where) ->
        if (target != null) {
            val message =
                if (where == arctic.view) "정말 이 아이템을 삭제하시겠어요?"
                else "${if (where == "inventory") "인벤토리" else "창고"}에 있는 아이템이에요. 정말 이 아이템을 삭제하시겠어요?"

            val description = "게임 내에서 분해하면 에메랄드 보상을 받을 수 있지만, 여기서는 받을 수 없어요."

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(80.dp))
                Row {
                    RetroButton("취소", Color(0xffffffff), "overlay") { arctic.deletion.target = null }
                    Spacer(modifier = Modifier.width(150.dp))
                    RetroButton("삭제", Color(0xffff6e25), "outline") { target.parent.deleteItem(target); arctic.deletion.target = null }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }

}

@Composable
fun RetroButton(
    text: String,
    color: Color,
    hoverInteraction: String,
    disabledColor: Color = Color(0xff666666),
    enabled: Boolean = true,
    buttonSize: Pair<Dp, Dp> = 225.dp to 70.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val pressed by source.collectIsPressedAsState()

    val radius = 8.dp.value
    val stroke = 5.dp.value

    val drawMain: DrawScope.() -> Unit = {
        drawRect(if (enabled) color else disabledColor, topLeft = Offset(stroke, stroke + radius), size = Size(size.width - 2 * stroke, size.height - 2 * (stroke + radius)))
        drawRect(if (enabled) color else disabledColor, topLeft = Offset(stroke + radius, stroke), size = Size(size.width - 2 * (stroke + radius), size.height - 2 * stroke))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(buttonSize.first, buttonSize.second)
            .hoverable(source, enabled)
            .clickable(source, null, enabled, onClick = onClick).then(modifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (hoverInteraction == "overlay") 0.2f else 1f)
                .drawBehind {
                    if (!hovered) return@drawBehind

                    if (hoverInteraction == "outline") {
                        drawRect(Color.White, topLeft = Offset(radius, 0f), size = Size(size.width - 2 * radius, size.height))
                        drawRect(Color.White, topLeft = Offset(0f, radius), size = Size(size.width, size.height - 2 * radius))
                    }

                    if (hoverInteraction == "overlay") {
                        drawMain()
                    }
                }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    if (hoverInteraction == "overlay") return@drawBehind

                    drawMain()
                }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.25f)
                .drawBehind {
                    if (!pressed) return@drawBehind

                    drawRect(Color.Black, topLeft = Offset(radius, 0f), size = Size(size.width - 2 * radius, size.height))
                    drawRect(Color.Black, topLeft = Offset(0f, radius), size = Size(size.width, size.height - 2 * radius))
                }
        )
        Text(text = text, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SavedPopup() {

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemCreationPopup() {
    val _enabled: Boolean = arctic.creation.enabled
    val _target: ItemData? = arctic.creation.target

    val _variant: String = arctic.creation.filter

    val blurRadius by animateDpAsState(if (_target != null) 75.dp else 0.dp)

    Backdrop(_enabled) { arctic.creation.disable() }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().blur(blurRadius)
    ) {
        Box {
            AnimatedCollection(_enabled, width = 160.dp, modifier = Modifier.offset(x = (-120).dp)) { enabled ->
                if (enabled) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.requiredWidth(160.dp).padding(top = 54.dp)) {
                        VariantFilterTextInteractable("근거리", "Melee")
                        VariantFilterTextInteractable("원거리", "Ranged")
                        VariantFilterTextInteractable("방어구", "Armor")
                        VariantFilterTextInteractable("유물", "Artifact")
                    }
                } else {
                    Spacer(modifier = Modifier.requiredWidth(160.dp).fillMaxHeight())
                }
            }
            AnimatedContent(
                targetState = _enabled to _variant,
                transitionSpec = {
                    val enter = fadeIn(tween(durationMillis = 250)) + slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(- 70.dp.value.toInt(), 0) })
                    var exit = fadeOut(tween(durationMillis = 250))
                    if (targetState.first) exit += scaleOut(tween(durationMillis = 250), targetScale = 0.9f)
                    enter with exit
                },
                modifier = Modifier.width(1050.dp)
            ) { (enabled, variant) ->
                if (enabled) ItemDataCollection(variant) { arctic.creation.target = it }
                else Box(modifier = Modifier.width(950.dp).fillMaxHeight())
            }
        }
    }

    Backdrop(_target != null) { arctic.creation.target = null }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedDetail(_target, modifier = Modifier.fillMaxWidth().height(550.dp)) { target ->
            if (target != null) ItemDataDetail(target)
            else Box(modifier = Modifier.fillMaxSize())
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ItemEditionPopup() {
    val _target: Item? = arctic.edition.target

    Backdrop(_target != null) { arctic.edition.disable() }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            AnimatedCollection(_target, width = 160.dp, modifier = Modifier.offset(x = (-120).dp)) { target ->
                if (target != null) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.requiredWidth(160.dp).padding(top = 54.dp)) {
                        VariantFilterTextInteractable("근거리", "Melee", target.data.variant == "Melee")
                        VariantFilterTextInteractable("원거리", "Ranged", target.data.variant == "Ranged")
                        VariantFilterTextInteractable("방어구", "Armor", target.data.variant == "Armor")
                        VariantFilterTextInteractable("유물", "Artifact", target.data.variant == "Artifact")
                    }
                } else {
                    Spacer(modifier = Modifier.requiredWidth(160.dp).fillMaxHeight())
                }
            }
            AnimatedContent(
                targetState = _target,
                transitionSpec = {
                    val enter = fadeIn(tween(durationMillis = 250)) + slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(- 70.dp.value.toInt(), 0) })
                    var exit = fadeOut(tween(durationMillis = 250))
                    if (targetState != null) exit += scaleOut(tween(durationMillis = 250), targetScale = 0.9f)
                    enter with exit
                },
                modifier = Modifier.width(1050.dp)
            ) { target ->
                if (target != null) ItemDataCollection(target.data.variant) {
                    target.type = it.type
                    arctic.edition.disable()
                }
                else Box(modifier = Modifier.width(950.dp).fillMaxHeight())
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VariantFilterTextInteractable(text: String, variant: String, enabled: Boolean = true) {
    val selected = arctic.creation.filter == variant
    val onClick = { arctic.creation.filter = variant }

    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()

    val blurAlpha by animateFloatAsState(if (!enabled) 0f else if (hovered) 0.8f else 0f)
    val overlayAlpha by animateFloatAsState(if (!enabled) 0.15f else if (selected) 1f else 0.6f)

    Box(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .hoverable(source, enabled = enabled)
            .onClick(matcher = PointerMatcher.mouse(PointerButton.Primary), enabled = enabled, onClick = onClick)
    ) {
        VariantFilterText(text, modifier = Modifier.blur(10.dp).alpha(blurAlpha))
        VariantFilterText(text, modifier = Modifier.alpha(overlayAlpha))
    }
}

@Composable
fun VariantFilterText(text: String, modifier: Modifier = Modifier) =
    Text(
        text = text,
        color = Color.White,
        fontSize = 35.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(start = 20.dp, top = 5.dp, bottom = 5.dp, end = 20.dp)
    )

@Composable
fun EnchantmentPopup() {
    val _target by remember { derivedStateOf { arctic.enchantments.detailTarget } }
    val _shadow by remember { derivedStateOf { arctic.enchantments.shadowDetailTarget } }

    val collectionTargetState = EnchantCollectionPopupTargetStates(
        holder = _target?.holder,
        index = _target?.holder?.enchantments?.indexOf(_target),
        isNetheriteEnchant = _target?.isNetheriteEnchant == true
    )
    val detailTargetState = EnchantDetailPopupTargetStates(
        target = _target,
        isUnset = _target?.id == "Unset"
    )

    val slideEnabled: (EnchantDetailPopupTargetStates, EnchantDetailPopupTargetStates) -> Boolean = { initial, target ->
        !initial.isUnset && target.target != null && !target.isUnset || initial.target == null && target.target != null
    }

    Backdrop(arctic.enchantments.hasDetailTarget) { arctic.enchantments.closeDetail() }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedCollection(collectionTargetState) { (holder, index, isNetheriteEnchant) ->
            if (holder != null && index != null) EnchantmentsCollection(holder, index, isNetheriteEnchant)
            else Box(modifier = Modifier.width(700.dp).fillMaxHeight())
        }
        AnimatedDetail(detailTargetState, slideEnabled = slideEnabled) { (target, isUnset) ->
            if (target != null && !isUnset) EnchantmentDetail(target)
            else Box(modifier = Modifier.width(0.dp).height(500.dp))

            if (target == null && _shadow != null && _shadow?.id != "Unset")
                Box(modifier = Modifier.width(675.dp).height(500.dp))
            else if (target == null && _shadow != null && _shadow?.id == "Unset")
                Box(modifier = Modifier.width(0.dp).height(500.dp))
        }
    }
}

data class EnchantCollectionPopupTargetStates(
    val holder: Item?,
    val index: Int?,
    val isNetheriteEnchant: Boolean
)

data class EnchantDetailPopupTargetStates(
    val target: Enchantment?,
    val isUnset: Boolean
)

@Composable
fun ArmorPropertyPopup() {
    val _target by remember { derivedStateOf { arctic.armorProperties.detailTarget } }
    val _into by remember { derivedStateOf { arctic.armorProperties.createInto } }
    val _created  by remember { derivedStateOf { arctic.armorProperties.created } }

    val _hasTarget = arctic.armorProperties.hasDetailTarget
    val _hasInto = arctic.armorProperties.hasCreateInto

    val collectionTargetState =
        if (_hasInto) {
            ArmorPropertyCollectionPopupTargetStates(
                holder = _into,
                index = _into?.armorProperties?.size
            )
        } else {
            val index = _target?.holder?.armorProperties?.indexOf(_target)
            ArmorPropertyCollectionPopupTargetStates(
                holder = _target?.holder,
                index = index
            )
        }

    val detailTargetState = ArmorPropertyDetailPopupTargetStates(
        holder = collectionTargetState.holder,
        target = _target,
        created = _created
    )

    val detailSizeTransformDuration: (ArmorPropertyDetailPopupTargetStates, ArmorPropertyDetailPopupTargetStates) -> Int = { initial, target ->
        if ((initial.holder == null && initial.created && !target.created) || (initial.holder == null && initial.target == null && target.created)) 0
        else 250
    }

    val slideEnabled: (ArmorPropertyDetailPopupTargetStates, ArmorPropertyDetailPopupTargetStates) -> Boolean = { initial, target ->
        initial.target != null && target.holder != null && target.target != null || initial.holder == null && target.holder != null
    }

    Backdrop(_hasTarget || _hasInto) {
        if (_hasTarget) arctic.armorProperties.closeDetail()
        else if (_hasInto) arctic.armorProperties.cancelCreation()
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedCollection(collectionTargetState) { (holder, index) ->
            val indexValid = index != null && index < (holder?.armorProperties?.size ?: 0)

            if (holder != null) ArmorPropertyCollection(holder, index, indexValid)
            else Box(modifier = Modifier.width(700.dp).fillMaxHeight())
        }

        AnimatedDetail(detailTargetState, slideEnabled = slideEnabled, sizeTransformDuration = detailSizeTransformDuration) { (_, target, created) ->
            if (target != null) ArmorPropertyDetail(target)
            else Box(modifier = Modifier.size(0.dp, 500.dp))

            if (target == null && created) {
                Box(modifier = Modifier.width(675.dp).height(500.dp))
            } else if (target == null) {
                Box(modifier = Modifier.width(0.dp).height(500.dp))
            }
        }
    }
}

data class ArmorPropertyCollectionPopupTargetStates(
    val holder: Item?,
    val index: Int?
)

data class ArmorPropertyDetailPopupTargetStates(
    val holder: Item?,
    val target: ArmorProperty?,
    val created: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Backdrop(visible: Boolean, alpha: Float = 0.3764f, onClick: () -> Unit) =
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), label = "Backdrop") {
        Box(modifier = Modifier.fillMaxSize().alpha(alpha).background(Color(0xff000000)).onClick(onClick = onClick))
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> AnimatedCollection(targetState: S, width: Dp = 750.dp, modifier: Modifier = Modifier, content: @Composable AnimatedVisibilityScope.(S) -> Unit) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val enter = fadeIn(tween(durationMillis = 250)) + slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(- 70.dp.value.toInt(), 0) })
            val exit = fadeOut(tween(durationMillis = 250))
            enter with exit
        },
        modifier = modifier.then(Modifier.width(width)),
        content = content
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> AnimatedDetail(
    targetState: S,
    sizeTransformDuration: (S, S) -> Int = { _, _, -> 250 },
    slideEnabled: (S, S) -> Boolean = { _, _ -> true },
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(S) -> Unit
) =
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val slide = slideEnabled(this.initialState, this.targetState)

            var enter = fadeIn(tween(durationMillis = 250))
            if (slide) enter += slideIn(tween(durationMillis = 250), initialOffset = { IntOffset(0, 50) })

            var exit = fadeOut(tween(durationMillis = 250))
            if (slide) exit += slideOut(tween(durationMillis = 250), targetOffset = { IntOffset(0, -50) })

            enter with exit using SizeTransform(false) { _, _ -> tween(durationMillis = sizeTransformDuration(this.initialState, this.targetState)) }
        },
        modifier = modifier.then(Modifier.height(500.dp)),
        content = content
    )
