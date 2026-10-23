package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.data.model.WishItem
import com.example.ui.AdminViewModel
import com.example.ui.clay.*

/**
 * Website-Matched Wishes & Celebrations Registry (`admin-wishes.html` 1:1):
 * - Supports status filters (`All`, `Published`, `Upcoming`, `Expired`, `Flagged`, `Suspended`)
 * - 1-Click `+7 Days`, `+30 Days`, `VIP Unlimited`, `Approve`, `Suspend`, `Transfer Ownership`, `Delete`, and `Open /w/:slug`
 */
@Composable
fun WishesScreen(
    viewModel: AdminViewModel,
    onlyExpiring: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val wishes by viewModel.wishes.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    var selectedStatus by remember(onlyExpiring) { mutableStateOf(if (onlyExpiring) "expired" else "All") }
    var showCreateWishModal by remember { mutableStateOf(false) }
    var transferTargetWish by remember { mutableStateOf<WishItem?>(null) }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    val filters = listOf("All", "published", "upcoming", "expired", "flagged", "suspended")
    val filtered = wishes.filter { w ->
        val matchStatus = when (selectedStatus) {
            "All" -> true
            "flagged" -> w.isFlagged || w.status.equals("flagged", true)
            else -> w.status.equals(selectedStatus, ignoreCase = true) || w.lifecycleStatus.equals(selectedStatus, ignoreCase = true)
        }
        val matchSearch = searchQuery.isBlank() ||
            w.recipientName.contains(searchQuery, ignoreCase = true) ||
            w.senderName.contains(searchQuery, ignoreCase = true) ||
            w.slug.contains(searchQuery, ignoreCase = true) ||
            w.templateName.contains(searchQuery, ignoreCase = true)
        matchStatus && matchSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Wish Lifecycle & Moderation • Live /w/:slug ✨",
                titlePrefix = if (onlyExpiring) "Expiring" else "Celebration",
                pinkAccentWord = "Wishes",
                titleSuffix = "(${filtered.size})",
                subtitle = "Inspect live customer wish links (/w/:slug), extend expiry (+7d / +30d / VIP), transfer ownership, moderate flagged content, or create a live test wish.",
                primaryButtonText = "+ Create Live Test Wish",
                primaryButtonIcon = Icons.Default.AutoAwesome,
                onPrimaryClick = { showCreateWishModal = true },
                secondaryButtonText = "↻ Sync Wishes",
                secondaryButtonIcon = Icons.Default.Refresh,
                onSecondaryClick = { viewModel.syncLive() }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { st ->
                    val active = selectedStatus == st
                    ClayButton(
                        onClick = { selectedStatus = st },
                        variant = if (active) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Text(st.uppercase(), fontSize = 10.5.sp)
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { wish ->
            val isHealthy = wish.status.equals("published", true) || wish.status.equals("active", true)
            TopAccentCard(
                accentBrush = if (isHealthy) {
                    Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary))
                } else {
                    Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Rose))
                },
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StatusBadgePill(
                                    text = wish.status.uppercase(),
                                    isSuccess = isHealthy,
                                    isDark = isDark
                                )
                                ClayPill(text = "👁 ${wish.viewsCount} views", color = ClayColors.Primary, isDark = isDark)
                                ClayPill(text = wish.relationship, color = ClayColors.Pink, isDark = isDark)
                                if (wish.isFlagged) {
                                    ClayPill(text = "🚩 FLAGGED", color = ClayColors.Rose, isDark = isDark)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "For ${wish.recipientName} — From ${wish.senderName}",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "Link: /w/${wish.slug} • Template: ${wish.templateName}",
                                fontSize = 11.5.sp,
                                color = mutedColor
                            )
                            Text(
                                text = "Created: ${wish.createdAt} • Expires: ${wish.expiresAt}",
                                fontSize = 10.5.sp,
                                color = mutedColor
                            )
                        }
                        CompactIconButton(onClick = { viewModel.deleteWish(wish.id, wish.slug) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ClayColors.Rose)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactIconButton(
                            onClick = {
                                val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$base/w/${wish.slug}")))
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Pink.copy(alpha = 0.16f))
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open Link", tint = ClayColors.Pink, modifier = Modifier.size(15.dp))
                        }

                        ClayButton(
                            onClick = { viewModel.extendWishExpiry(wish.id, wish.slug, 7) },
                            variant = ClayButtonVariant.EMERALD,
                            isDark = isDark
                        ) {
                            Text("+7d", fontSize = 10.sp)
                        }

                        ClayButton(
                            onClick = { viewModel.extendWishExpiry(wish.id, wish.slug, 30) },
                            variant = ClayButtonVariant.SKY,
                            isDark = isDark
                        ) {
                            Text("+30d", fontSize = 10.sp)
                        }

                        ClayButton(
                            onClick = { viewModel.extendWishExpiry(wish.id, wish.slug, 365) },
                            variant = ClayButtonVariant.AMBER,
                            isDark = isDark
                        ) {
                            Text("+365d", fontSize = 10.sp)
                        }

                        CompactIconButton(
                            onClick = { transferTargetWish = wish },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Primary.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer", tint = ClayColors.Primary, modifier = Modifier.size(16.dp))
                        }

                        CompactIconButton(
                            onClick = { viewModel.moderateWish(wish.id, wish.slug, "approve") },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Emerald.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Approve", tint = ClayColors.Emerald, modifier = Modifier.size(16.dp))
                        }

                        CompactIconButton(
                            onClick = { viewModel.moderateWish(wish.id, wish.slug, "suspend") },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Rose.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.Block, contentDescription = "Suspend", tint = ClayColors.Rose, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    transferTargetWish?.let { wish ->
        var targetEmail by remember(wish) { mutableStateOf("") }
        Dialog(onDismissRequest = { transferTargetWish = null }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Transfer Wish Ownership",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = "Wish: /w/${wish.slug} (For ${wish.recipientName})",
                        fontSize = 11.5.sp,
                        color = mutedColor
                    )
                    ClayTextField(
                        value = targetEmail,
                        onValueChange = { targetEmail = it },
                        label = "New Owner Customer Email",
                        placeholder = "customer@example.com",
                        isDark = isDark
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { transferTargetWish = null }) { Text("Cancel", color = mutedColor) }
                        Spacer(Modifier.width(8.dp))
                        ClayButton(
                            onClick = {
                                if (targetEmail.isNotBlank()) {
                                    viewModel.transferWishOwnership(wish.id, wish.slug, targetEmail)
                                    transferTargetWish = null
                                }
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Text("Transfer Ownership", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCreateWishModal) {
        var recipientName by remember { mutableStateOf("Aarav & Family") }
        var message by remember { mutableStateOf("Wishing you a radiant celebration filled with joy and prosperity!") }
        var templateId by remember { mutableStateOf(templates.firstOrNull()?.id ?: "tpl_glitter_birthday") }
        var relationship by remember { mutableStateOf("family") }

        Dialog(onDismissRequest = { showCreateWishModal = false }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Create Live Test Wish in D1", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    ClayTextField(value = recipientName, onValueChange = { recipientName = it }, label = "Recipient Name", isDark = isDark)
                    ClayTextField(value = message, onValueChange = { message = it }, label = "Celebration Message", isDark = isDark)
                    ClayTextField(value = templateId, onValueChange = { templateId = it }, label = "Template ID", isDark = isDark)
                    ClayTextField(value = relationship, onValueChange = { relationship = it }, label = "Relationship Slug", isDark = isDark)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateWishModal = false }) { Text("Cancel", color = mutedColor) }
                        Spacer(Modifier.width(8.dp))
                        ClayButton(
                            onClick = {
                                viewModel.createTestWish(recipientName, message, templateId, relationship)
                                showCreateWishModal = false
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Text("Publish Live Wish", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
