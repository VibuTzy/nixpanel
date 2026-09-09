package com.example.nixeon.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    enableGlowOrbs: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_orbs")
    val orbOffset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_float"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        NixeonObsidian,
                        Color(0xFF0C1B2E),
                        Color(0xFF140D25)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1800f)
                )
            )
    ) {
        if (enableGlowOrbs) {
            // Neon Cyan ambient orb top-left
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(x = (-80).dp + orbOffset.dp, y = (40).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NixeonCyan.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Deep Violet ambient orb bottom-right
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (80).dp - orbOffset.dp, y = (60).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NixeonViolet.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        content()
    }
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.075f),
    borderColor: Color = Color.White.copy(alpha = 0.18f),
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    val tagModifier = if (testTag != null) {
        Modifier.testTag(testTag)
    } else Modifier

    Column(
        modifier = modifier
            .then(tagModifier)
            .clip(shape)
            .then(clickModifier)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.35f),
                        borderColor.copy(alpha = 0.08f)
                    )
                ),
                shape = shape
            )
            .drawWithCache {
                val shine = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.Transparent,
                        NixeonCyan.copy(alpha = 0.04f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width * 0.8f, size.height)
                )
                onDrawWithContent {
                    drawRoundRect(
                        brush = shine,
                        cornerRadius = CornerRadius(24.dp.toPx())
                    )
                    drawContent()
                }
            }
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun LiquidPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    testTag: String = "liquid_primary_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = NixeonCyan.copy(alpha = 0.2f),
            contentColor = NixeonObsidian,
            disabledContentColor = NixeonTextMuted
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled && !loading) {
                        Brush.horizontalGradient(
                            colors = listOf(NixeonCyan, Color(0xFF6DE8C8))
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(NixeonCyan.copy(alpha = 0.3f), Color(0xFF6DE8C8).copy(alpha = 0.3f))
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = NixeonObsidian
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = NixeonObsidian,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NixeonObsidian
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    borderColor: Color = NixeonGlassBorder,
    textColor: Color = NixeonTextPrimary,
    testTag: String = "liquid_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = textColor,
            containerColor = Color.White.copy(alpha = 0.04f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            )
        }
    }
}

@Composable
fun LiquidBadge(
    text: String,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color = NixeonObsidian
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = badgeColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun AdminModeBanner(
    modifier: Modifier = Modifier,
    adminSlotName: String
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF3B1E12).copy(alpha = 0.85f),
        border = BorderStroke(1.dp, NixeonAmber.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin Mode",
                tint = NixeonAmber,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MODE ADMINISTRATOR AKTIF",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NixeonAmber,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = "Sesi operasional resmi: $adminSlotName. Seluruh aksi diaudit ke server ledger.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = NixeonTextSecondary
                    )
                )
            }
        }
    }
}

@Composable
fun SyncTimestampBadge(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val timeStr = remember(timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.format(Date(timestamp))
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NixeonEmerald)
        )
        Text(
            text = "Sinkronisasi Gateway: $timeStr WIB",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                color = NixeonTextMuted
            )
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier,
        contentPadding = 14.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(color = NixeonTextSecondary)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = NixeonTextPrimary
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                color = NixeonTextMuted,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun ConfirmationDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmLabel: String = "Lanjutkan",
    cancelLabel: String = "Batal",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isDestructive) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NixeonCrimson
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = NixeonTextSecondary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDestructive) NixeonCrimson else NixeonCyan,
                        contentColor = NixeonObsidian
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(confirmLabel, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = NixeonTextSecondary)
                ) {
                    Text(cancelLabel)
                }
            },
            containerColor = NixeonSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    valueColor: Color = NixeonTextPrimary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = valueColor,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

