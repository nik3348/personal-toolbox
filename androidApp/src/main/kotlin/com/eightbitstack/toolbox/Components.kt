package com.eightbitstack.toolbox

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun Kicker(
    text: String,
    color: Color = ToolboxTheme.ok,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        fontFamily = ToolboxTheme.mono,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        color = color,
        modifier = modifier
    )
}

@Composable
fun ChunkyButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    variant: String = "primary", // "primary", "outline", "ghost"
    size: String = "md", // "sm", "md", "lg"
    icon: (@Composable () -> Unit)? = null,
    fullWidth: Boolean = false,
    shadowColor: Color? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val actualPressed = isPressed && enabled

    val palette = ToolboxTheme.activePalette
    val sc = if (enabled) (shadowColor ?: if (variant == "primary") palette.deep else (if (LocalDarkMode.current) Color(0xFF090D16) else ToolboxTheme.ink)) else Color.Transparent

    val height = when (size) {
        "sm" -> 32.dp
        "lg" -> 52.dp
        else -> 44.dp
    }

    val paddingValues = when (size) {
        "sm" -> PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        "lg" -> PaddingValues(horizontal = 22.dp, vertical = 14.dp)
        else -> PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    }

    val fontSize = when (size) {
        "sm" -> 11.sp
        "lg" -> 15.sp
        else -> 13.sp
    }

    val bg = if (enabled) {
        when (variant) {
            "primary" -> palette.primary
            "ghost" -> Color.Transparent
            else -> ToolboxTheme.surface
        }
    } else {
        if (variant == "ghost") Color.Transparent else ToolboxTheme.bgSubtle
    }

    val textColor = if (enabled) {
        when (variant) {
            "primary" -> Color.White
            else -> ToolboxTheme.ink
        }
    } else {
        ToolboxTheme.inkMute
    }

    val border = if (enabled) {
        when (variant) {
            "outline" -> Modifier.border(2.dp, if (LocalDarkMode.current) Color(0xFF334155) else ToolboxTheme.ink, RoundedCornerShape(12.dp))
            "ghost" -> Modifier
            else -> Modifier.border(1.5.dp, if (variant == "primary") palette.primary else (if (LocalDarkMode.current) Color(0xFF334155) else ToolboxTheme.ink), RoundedCornerShape(12.dp))
        }
    } else {
        if (variant == "ghost") Modifier else Modifier.border(1.5.dp, ToolboxTheme.line, RoundedCornerShape(12.dp))
    }

    val offset = if (actualPressed) 4.dp else 0.dp
    val shadowOffset = if (actualPressed) 0.dp else 4.dp

    Box(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Shadow (drawn underneath offset button)
        if (variant != "ghost" && enabled && shadowOffset > 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 4.dp, top = 4.dp)
                    .background(sc, RoundedCornerShape(12.dp))
            )
        }

        // Button body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = if (actualPressed) 0.dp else 4.dp, bottom = if (actualPressed) 0.dp else 4.dp)
                .offset(x = offset, y = offset)
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .then(border)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.wrapContentSize()
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text.uppercase(),
                    color = textColor,
                    fontFamily = ToolboxTheme.mono,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val mod = modifier
        .clip(RoundedCornerShape(16.dp))
        .background(ToolboxTheme.surface)
        .then(if (border) Modifier.border(1.dp, ToolboxTheme.line, RoundedCornerShape(16.dp)) else Modifier)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(modifier = mod, content = content)
}

@Composable
fun Toggle(
    value: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: String = "md"
) {
    val W = if (size == "sm") 36.dp else 44.dp
    val H = if (size == "sm") 22.dp else 26.dp
    val dot = H - 4.dp
    val palette = ToolboxTheme.activePalette

    Box(
        modifier = modifier
            .size(W, H)
            .clip(RoundedCornerShape(H))
            .background(if (value) palette.primary else ToolboxTheme.control)
            .clickable { onChange(!value) }
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(dot)
                .offset(x = if (value) W - dot - 4.dp else 0.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
        )
    }
}

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = ToolboxTheme.inkMute,
    bg: Color = ToolboxTheme.bgSubtle,
    icon: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = text.uppercase(),
            fontFamily = ToolboxTheme.mono,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = color
        )
    }
}

@Composable
fun FilterChip(
    active: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) ToolboxTheme.ink else ToolboxTheme.surface)
            .border(1.5.dp, if (active) ToolboxTheme.ink else ToolboxTheme.line, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = ToolboxTheme.mono,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = if (active) ToolboxTheme.bg else ToolboxTheme.ink
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (active) Color(0x2EFFFFFF) else ToolboxTheme.bgSubtle)
                    .padding(horizontal = 5.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 10.sp,
                    color = if (active) ToolboxTheme.bg else ToolboxTheme.inkMute
                )
            }
        }
    }
}

@Composable
fun Field(
    label: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(bottom = 14.dp)) {
        Kicker(text = label, color = ToolboxTheme.inkMute, modifier = Modifier.padding(bottom = 6.dp))
        content()
        if (hint != null) {
            Text(
                text = hint,
                fontSize = 11.sp,
                color = ToolboxTheme.inkMute,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TextInput(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                1.5.dp,
                if (isFocused) ToolboxTheme.activePalette.primary else ToolboxTheme.line,
                RoundedCornerShape(10.dp)
            )
            .background(ToolboxTheme.bg, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusRequester.requestFocus()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(
                fontFamily = ToolboxTheme.sans,
                fontSize = 15.sp,
                color = ToolboxTheme.ink
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 15.sp,
                        color = ToolboxTheme.inkMute,
                        fontFamily = ToolboxTheme.sans,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun Sheet(
    open: Boolean,
    onClose: () -> Unit,
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isRendered by remember { mutableStateOf(open) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val offsetY = remember { androidx.compose.animation.core.Animatable(if (open) 0f else 1500f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(open) {
        if (open) {
            isRendered = true
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
            )
        } else {
            // Smooth fluid close
            offsetY.animateTo(
                targetValue = 2000f, 
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            isRendered = false
        }
    }

    if (!isRendered && offsetY.value >= 1999f) return

    // Scrim disappears fluidly as sheet moves down
    val scrimAlpha = (1f - (offsetY.value / 1000f).coerceIn(0f, 1f)) * 0.4f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A).copy(alpha = scrimAlpha))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    onClose()
                })
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .offset(y = with(density) { offsetY.value.toDp() })
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetY.value > 250f) {
                                // Animate away fluidly for local feedback
                                scope.launch {
                                    offsetY.animateTo(2000f, spring(stiffness = Spring.StiffnessMediumLow))
                                }
                                onClose()
                            } else {
                                scope.launch {
                                    offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy))
                                }
                            }
                        },
                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                            change.consume()
                            scope.launch {
                                val newVal = (offsetY.value + dragAmount.y).coerceAtLeast(0f)
                                offsetY.snapTo(newVal)
                            }
                        }
                    )
                }
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(ToolboxTheme.surface)
                .border(1.dp, ToolboxTheme.line, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { /* Prevent clicks through sheet */ })
                }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .size(36.dp, 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (LocalDarkMode.current) Color(0xFF475569) else Color(0xFFCBD5E1))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(14.dp))
                if (title != null) {
                    Text(
                        text = title,
                        fontFamily = ToolboxTheme.serif,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ToolboxTheme.ink,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }
                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ToolboxMark(
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Text(
        text = "🕹",
        fontSize = size.value.sp,
        modifier = modifier
    )
}

// Vector Icon Canvas definitions to avoid drawing external PNGs
@Composable
fun IconBell(color: Color = ToolboxTheme.ink, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val path = Path().apply {
            moveTo(size.toPx() * 0.25f, size.toPx() * 0.75f)
            lineTo(size.toPx() * 0.75f, size.toPx() * 0.75f)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun IconHome(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        drawRect(color, size = this.size, style = Stroke(2.dp.toPx()))
    }
}
