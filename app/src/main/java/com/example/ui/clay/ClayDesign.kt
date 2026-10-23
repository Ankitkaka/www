package com.example.ui.clay

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * WishMagic Compact & Ergonomic Admin Studio Design System
 */
object ClayColors {
    // Website Light Mode Palette
    val LightBg = Color(0xFFF3EFFA)
    val LightBgEnd = Color(0xFFEBE4F8)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceMid = Color(0xFFFAF8FF)
    val LightSurfaceEnd = Color(0xFFF3EEFC)
    val LightWell = Color(0xFFF4EFFC)
    val LightWellDeep = Color(0xFFE9E1F8)
    val LightBorder = Color(0xFFDED4F2)
    val LightText = Color(0xFF1A0F30)
    val LightTextMuted = Color(0xFF533F78)

    // Website Cosmic Dark Mode Palette
    val DarkBg = Color(0xFF0F0822)
    val DarkBgEnd = Color(0xFF0B0618)
    val DarkSurface = Color(0xFF1F123E)
    val DarkSurfaceMid = Color(0xFF1B1038)
    val DarkSurfaceEnd = Color(0xFF140B29)
    val DarkWell = Color(0xFF2A1952)
    val DarkWellDeep = Color(0xFF180E32)
    val DarkBorder = Color(0xFF48327A)
    val DarkText = Color(0xFFF7F4FF)
    val DarkTextMuted = Color(0xFFCFC2EB)

    // Compact Section Banner Colors
    val HeroBgStart = Color(0xFF180B38)
    val HeroBgMid = Color(0xFF2B145E)
    val HeroBgEnd = Color(0xFF461D80)

    // Brand Accents
    val PrimaryLight = Color(0xFFA855F7)
    val Primary = Color(0xFF7C3AED)
    val PrimaryDark = Color(0xFF5B21B6)

    val PinkLight = Color(0xFFFF3B9A)
    val Pink = Color(0xFFEC4899)
    val PinkDark = Color(0xFFBE185D)

    val EmeraldLight = Color(0xFF34D399)
    val Emerald = Color(0xFF10B981)
    val EmeraldDark = Color(0xFF047857)

    val AmberLight = Color(0xFFFBBF24)
    val Amber = Color(0xFFF59E0B)
    val AmberDark = Color(0xFFB45309)

    val SkyLight = Color(0xFF38BDF8)
    val Sky = Color(0xFF0284C7)
    val SkyDark = Color(0xFF0369A1)

    val RoseLight = Color(0xFFFB7185)
    val Rose = Color(0xFFE11D48)
    val RoseDark = Color(0xFF9F1239)
}

fun Modifier.clay3DBevel(
    cornerRadius: Dp = 16.dp,
    isDark: Boolean = false,
    isRecessedWell: Boolean = false,
    tintBevelColor: Color? = null
): Modifier = this.drawWithContent {
    val w = size.width
    val h = size.height
    val r = cornerRadius.toPx()

    if (!isRecessedWell && w > 0f && h > 0f) {
        val domeRadius = (maxOf(w, h) * 0.85f).coerceAtLeast(1f)
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (!isDark) Color(0x33FFFFFF) else Color(0x14FFFFFF),
                    Color.Transparent
                ),
                center = Offset(w * 0.22f, h * 0.16f),
                radius = domeRadius
            ),
            cornerRadius = CornerRadius(r, r)
        )
    }

    drawContent()

    if (w <= 0f || h <= 0f) return@drawWithContent

    if (isRecessedWell) {
        val innerShadowStroke = 1.5.dp.toPx()
        val half = innerShadowStroke / 2f
        val cavityDark = if (!isDark) Color(0x28654BA3) else Color(0x99030109)
        val cavityLight = if (!isDark) Color(0xCCFFFFFF) else Color(0x18FFFFFF)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(cavityDark, Color.Transparent, cavityLight),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            topLeft = Offset(half, half),
            size = Size((w - innerShadowStroke).coerceAtLeast(0f), (h - innerShadowStroke).coerceAtLeast(0f)),
            cornerRadius = CornerRadius(r, r),
            style = Stroke(width = innerShadowStroke)
        )
    } else {
        val cushionStroke = 1.5.dp.toPx()
        val halfCushion = cushionStroke / 2f
        val bottomRightShadow = tintBevelColor ?: if (!isDark) Color(0x227B5EC7) else Color(0x88060312)
        val topLeftSpecular = if (!isDark) Color(0xEEFFFFFF) else Color(0x28FFFFFF)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    topLeftSpecular,
                    Color.Transparent,
                    bottomRightShadow
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            topLeft = Offset(halfCushion, halfCushion),
            size = Size((w - cushionStroke).coerceAtLeast(0f), (h - cushionStroke).coerceAtLeast(0f)),
            cornerRadius = CornerRadius(r, r),
            style = Stroke(width = cushionStroke)
        )
    }
}

/**
 * ULTRA-COMPACT SECTION HEADER BAR
 * Replaces the old tall hero banner across all Android app sections so it takes minimal vertical space,
 * eliminates long multi-line paragraphs, and renders non-overlapping single-line action buttons.
 */
@Composable
fun CosmicHeroBanner(
    welcomePillText: String,
    titlePrefix: String,
    pinkAccentWord: String,
    titleSuffix: String = "",
    subtitle: String = "",
    primaryButtonText: String? = null,
    primaryButtonIcon: ImageVector? = null,
    onPrimaryClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    secondaryButtonIcon: ImageVector? = null,
    onSecondaryClick: (() -> Unit)? = null,
    tertiaryButtonText: String? = null,
    onTertiaryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val hasButtons = (primaryButtonText != null && onPrimaryClick != null) ||
        (secondaryButtonText != null && onSecondaryClick != null) ||
        (tertiaryButtonText != null && onTertiaryClick != null)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, shape, spotColor = Color(0x444A1D7C))
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        ClayColors.HeroBgStart,
                        ClayColors.HeroBgMid,
                        ClayColors.HeroBgEnd
                    )
                )
            )
            .border(1.dp, Color(0x33FFFFFF), shape)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Row 1: Single-line Title + Compact Status Pill (padded on left/right so fixed top corner floating buttons never cover text)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp, end = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                            append(titlePrefix)
                        }
                        if (pinkAccentWord.isNotBlank()) {
                            append(" ")
                            withStyle(
                                SpanStyle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF4D9D), Color(0xFFF472B6), Color(0xFFFBBF24))
                                    ),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append(pinkAccentWord)
                            }
                        }
                        if (titleSuffix.isNotBlank()) {
                            append(" ")
                            withStyle(SpanStyle(color = Color(0xFFE9D5FF), fontWeight = FontWeight.Bold)) {
                                append(titleSuffix)
                            }
                        }
                    },
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                // Compact Live D1 Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x3310B981))
                        .border(1.dp, Color(0x5534D399), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399))
                        )
                        Text(
                            text = "LIVE D1",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Row 2: Horizontally Scrollable Compact Single-Line Action Buttons (Never Wraps or Overlaps!)
            if (hasButtons) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (primaryButtonText != null && onPrimaryClick != null) {
                        val btnShape = RoundedCornerShape(9.dp)
                        Box(
                            modifier = Modifier
                                .clip(btnShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF2A85), Color(0xFFEC4899), Color(0xFFA855F7))
                                    )
                                )
                                .border(1.dp, Color(0x55FFFFFF), btnShape)
                                .clickable(onClick = onPrimaryClick)
                                .padding(horizontal = 10.dp, vertical = 5.5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (primaryButtonIcon != null) {
                                    Icon(
                                        imageVector = primaryButtonIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = primaryButtonText,
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    if (secondaryButtonText != null && onSecondaryClick != null) {
                        val btnShape = RoundedCornerShape(9.dp)
                        Box(
                            modifier = Modifier
                                .clip(btnShape)
                                .background(Color(0x26FFFFFF))
                                .border(1.dp, Color(0x4DFFFFFF), btnShape)
                                .clickable(onClick = onSecondaryClick)
                                .padding(horizontal = 10.dp, vertical = 5.5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (secondaryButtonIcon != null) {
                                    Icon(
                                        imageVector = secondaryButtonIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = secondaryButtonText,
                                    color = Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    if (tertiaryButtonText != null && onTertiaryClick != null) {
                        val btnShape = RoundedCornerShape(9.dp)
                        Box(
                            modifier = Modifier
                                .clip(btnShape)
                                .background(Color(0x1FFFFFFF))
                                .border(1.dp, Color(0x3DFFFFFF), btnShape)
                                .clickable(onClick = onTertiaryClick)
                                .padding(horizontal = 9.dp, vertical = 5.5.dp)
                        ) {
                            Text(
                                text = tertiaryButtonText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Website-Matched KPI Stat Card with Area Sparkline Chart
 */
@Composable
fun SparklineKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector? = null,
    symbolText: String? = null,
    iconBrush: Brush,
    sparklineColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    ClayCard(
        modifier = modifier.clickable(onClick = onClick),
        isDark = isDark,
        shape = RoundedCornerShape(16.dp),
        cornerRadius = 16.dp,
        elevation = 3.dp,
        contentPadding = PaddingValues(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBrush),
                    contentAlignment = Alignment.Center
                ) {
                    if (symbolText != null) {
                        Text(
                            text = symbolText,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = mutedColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayColors.Emerald,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .width(42.dp)
                    .height(24.dp)
            ) {
                val w = size.width
                val h = size.height
                val pts = listOf(
                    Offset(0f, h * 0.85f),
                    Offset(w * 0.22f, h * 0.68f),
                    Offset(w * 0.45f, h * 0.54f),
                    Offset(w * 0.68f, h * 0.58f),
                    Offset(w * 0.85f, h * 0.28f),
                    Offset(w, h * 0.14f)
                )
                val linePath = Path().apply {
                    moveTo(pts.first().x, pts.first().y)
                    for (i in 1 until pts.size) {
                        val prev = pts[i - 1]
                        val cur = pts[i]
                        val midX = (prev.x + cur.x) / 2f
                        cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
                    }
                }
                val areaPath = Path().apply {
                    addPath(linePath)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(sparklineColor.copy(alpha = 0.32f), Color.Transparent)
                    )
                )
                drawPath(
                    path = linePath,
                    color = sparklineColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * Website-Matched Card with Top Colored Gradient Accent Strip
 */
@Composable
fun TopAccentCard(
    accentBrush: Brush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
    isDark: Boolean = false,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val bgColor = if (!isDark) Color.White else ClayColors.DarkSurface
    val borderColor = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder

    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = if (!isDark) Color(0x2237196E) else Color(0xAA000000),
                spotColor = if (!isDark) Color(0x337C3AED) else Color(0xDD000000)
            )
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.5.dp)
                .background(accentBrush)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Full-Width Gradient Action Button with crisp rounded corners (`11.dp`)
 */
@Composable
fun GradientPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    brush: Brush = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFEC4899)))
) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = modifier
            .shadow(4.dp, shape, spotColor = Color(0x55EC4899))
            .clip(shape)
            .background(brush)
            .border(1.dp, Color(0x44FFFFFF), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Status Badge Pill
 */
@Composable
fun StatusBadgePill(
    text: String,
    isSuccess: Boolean = true,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bg = if (isSuccess) {
        if (!isDark) Color(0xFFD1FAE5) else Color(0x3810B981)
    } else {
        if (!isDark) Color(0xFFFFE4E6) else Color(0x38F43F5E)
    }
    val fg = if (isSuccess) {
        if (!isDark) Color(0xFF047857) else Color(0xFF6EE7B7)
    } else {
        if (!isDark) Color(0xFFBE123C) else Color(0xFFFDA4AF)
    }
    val borderCol = if (isSuccess) {
        if (!isDark) Color(0xFFA7F3D0) else Color(0x6610B981)
    } else {
        if (!isDark) Color(0xFFFECDD3) else Color(0x66F43F5E)
    }

    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, borderCol, shape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = text,
                color = fg,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Clean Website-Matched Card
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    cornerRadius: Dp = 16.dp,
    isDark: Boolean = false,
    elevation: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val bgBrush = if (!isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFAF8FF)),
            start = Offset(0f, 0f),
            end = Offset(700f, 700f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF231546), ClayColors.DarkSurface, ClayColors.DarkSurfaceMid),
            start = Offset(0f, 0f),
            end = Offset(700f, 700f)
        )
    }
    val borderColor = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = if (!isDark) Color(0x2837196E) else Color(0xCC000000),
                spotColor = if (!isDark) Color(0x337C3AED) else Color(0xEE000000)
            )
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, borderColor, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun ClayWellBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    cornerRadius: Dp = 12.dp,
    isDark: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(10.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val wellBg = if (!isDark) ClayColors.LightWell else ClayColors.DarkWell
    val innerBorder = if (!isDark) Color(0xFFDFD4F4) else Color(0xFF48327A)

    Box(
        modifier = modifier
            .clip(shape)
            .background(wellBg)
            .border(1.dp, innerBorder, shape)
            .padding(contentPadding),
        content = content
    )
}

enum class ClayButtonVariant {
    PRIMARY, PINK, EMERALD, AMBER, SKY, DANGER, SECONDARY
}

/**
 * Clean Non-Overlapping Button with crisp `10.dp` rounded corners.
 * Never wraps into tall ovals or overlaps adjacent buttons.
 */
@Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ClayButtonVariant = ClayButtonVariant.PRIMARY,
    isDark: Boolean = false,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.5.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "clay_btn_scale"
    )

    val (brush, textColor, borderCol) = when (variant) {
        ClayButtonVariant.PRIMARY -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFEC4899))),
            Color.White,
            Color(0x44FFFFFF)
        )
        ClayButtonVariant.PINK -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFFFF2A85), Color(0xFFEC4899), Color(0xFFA855F7))),
            Color.White,
            Color(0x44FFFFFF)
        )
        ClayButtonVariant.EMERALD -> Triple(
            Brush.horizontalGradient(listOf(ClayColors.EmeraldLight, ClayColors.Emerald, ClayColors.EmeraldDark)),
            Color.White,
            Color(0x44FFFFFF)
        )
        ClayButtonVariant.AMBER -> Triple(
            Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEC4899))),
            Color.White,
            Color(0x44FFFFFF)
        )
        ClayButtonVariant.SKY -> Triple(
            Brush.horizontalGradient(listOf(ClayColors.SkyLight, ClayColors.Sky, ClayColors.SkyDark)),
            Color.White,
            Color(0x44FFFFFF)
        )
        ClayButtonVariant.DANGER -> Triple(
            Brush.horizontalGradient(listOf(ClayColors.RoseLight, ClayColors.Rose, ClayColors.RoseDark)),
            Color.White,
            Color(0x44FFFFFF)
        )
        ClayButtonVariant.SECONDARY -> Triple(
            if (!isDark) Brush.linearGradient(listOf(Color(0xFFF6F2FD), Color(0xFFEEE6FC)))
            else Brush.linearGradient(listOf(Color(0xFF2B1A54), Color(0xFF211342))),
            if (!isDark) ClayColors.LightText else ClayColors.DarkText,
            if (!isDark) Color(0xFFDFD3F6) else Color(0xFF48327A)
        )
    }

    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 31.dp)
            .clip(shape)
            .background(brush)
            .border(1.dp, borderCol, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        ProvideTextStyle(
            value = MaterialTheme.typography.labelLarge.copy(
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.5.sp,
                lineHeight = 13.sp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

@Composable
fun ClaySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "clay_switch_thumb"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) ClayColors.Pink else (if (!isDark) ClayColors.LightWellDeep else ClayColors.DarkWellDeep),
        label = "clay_switch_track"
    )

    Box(
        modifier = modifier
            .width(42.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (checked) {
                    Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink))
                } else {
                    Brush.linearGradient(listOf(trackColor, trackColor))
                }
            )
            .border(1.dp, if (checked) Color(0x88FFFFFF) else Color(0x337C3AED), RoundedCornerShape(999.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(18.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun ClayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    isDark: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isFocused) ClayColors.Primary else mutedColor,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val shape = RoundedCornerShape(11.dp)
        val inputBg = if (!isDark) Color.White else Color(0xFF140B2A)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 36.dp)
                .clip(shape)
                .background(inputBg)
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) ClayColors.Primary else (if (!isDark) Color(0xFFD5C7F0) else Color(0xFF48327A)),
                    shape = shape
                )
                .padding(horizontal = 10.dp, vertical = 7.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Box(modifier = Modifier.size(15.dp), contentAlignment = Alignment.Center) {
                        leadingIcon()
                    }
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                        Text(
                            text = placeholder,
                            fontSize = 11.5.sp,
                            color = mutedColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = singleLine,
                        keyboardOptions = keyboardOptions,
                        visualTransformation = visualTransformation,
                        cursorBrush = SolidColor(ClayColors.Primary),
                        textStyle = TextStyle(
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused }
                    )
                }
            }
        }
    }
}

@Composable
fun ClayPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = ClayColors.Primary,
    isDark: Boolean = false
) {
    val bgColor = color.copy(alpha = if (!isDark) 0.14f else 0.24f)
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.38f), shape)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (!isDark) color else color.copy(alpha = 0.98f),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ClayOrbIcon(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush = Brush.linearGradient(listOf(ClayColors.PrimaryLight, ClayColors.Primary, ClayColors.PrimaryDark)),
    size: Dp = 34.dp
) {
    val r = (size.value * 0.32f).dp
    val shape = RoundedCornerShape(r)
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, shape, spotColor = Color(0x667C3AED))
            .clip(shape)
            .background(brush)
            .border(1.dp, Color(0x88FFFFFF), shape),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}


/**
 * Compact Non-Overlapping Icon Button
 * Uses a pure Box without Material3 minimumInteractiveComponentSize (48.dp) expansion,
 * so adjacent icon buttons keep their exact visual bounds and never bleed into or overlap each other.
 */
@Composable
fun CompactIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
        .size(32.dp)
        .clip(RoundedCornerShape(9.dp)),
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}
