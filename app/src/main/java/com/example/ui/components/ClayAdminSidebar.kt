package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminNavGroup
import com.example.data.model.AdminSection
import com.example.ui.clay.*

fun resolveSectionIcon(section: AdminSection): ImageVector = when (section) {
    AdminSection.OVERVIEW -> Icons.Default.Home
    AdminSection.TEMPLATES -> Icons.Default.GridView
    AdminSection.ADD_TEMPLATE -> Icons.Default.AddCircle
    AdminSection.FESTIVALS -> Icons.Default.EventAvailable
    AdminSection.OCCASIONS -> Icons.Default.Category
    AdminSection.MEDIA -> Icons.Default.Image
    AdminSection.MUSIC -> Icons.Default.MusicNote
    AdminSection.WISHES -> Icons.Default.Description
    AdminSection.EXPIRING -> Icons.Default.Schedule
    AdminSection.ANALYTICS -> Icons.Default.TrendingUp
    AdminSection.USERS -> Icons.Default.Group
    AdminSection.PRICING -> Icons.Default.WorkspacePremium
    AdminSection.ORDERS -> Icons.Default.CreditCard
    AdminSection.SETTINGS -> Icons.Default.Settings
    AdminSection.STORAGE_HEALTH -> Icons.Default.Storage
    AdminSection.BACKUPS -> Icons.Default.CloudDownload
    AdminSection.AUDIT_LOGS -> Icons.Default.ReceiptLong
    AdminSection.USER_CREATIONS -> Icons.Default.FolderShared
    AdminSection.SEARCH -> Icons.Default.ManageSearch
    AdminSection.REFERRALS -> Icons.Default.CardGiftcard
    AdminSection.MODERATION -> Icons.Default.Gavel
    AdminSection.SUPPORT -> Icons.Default.SupportAgent
    AdminSection.STAFF -> Icons.Default.Badge
    AdminSection.LEGAL -> Icons.Default.Policy
    AdminSection.ANNOUNCEMENTS -> Icons.Default.Campaign
    AdminSection.NOTIFICATIONS -> Icons.Default.NotificationsActive
}

/**
 * Website-Matched Cosmic Fairytale Castle Left Sidebar (`admin-overview.html` & `admin-theme.css` 1:1).
 * Features:
 * - Magic Wand glowing icon + `WishMagic` (`Magic` in pink-gold italic gradient) + `Create. Personalize. Celebrate.`
 * - Active item highlighted with Glowing Pink-Magenta-Purple Pill (`#FF2A85` -> `#EC4899` -> `#A855F7`)
 * - Expandable `More Studio Sections` group keeping all 26 Admin sections accessible
 */
@Composable
fun ClayAdminSidebar(
    selectedSection: AdminSection,
    isDark: Boolean,
    templatesCount: Int,
    wishesCount: Int,
    usersCount: Int,
    ordersCount: Int,
    hasActiveFestival: Boolean,
    onSelectSection: (AdminSection) -> Unit,
    onOpenLiveWebsite: () -> Unit,
    onCloseDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var moreExpanded by remember {
        mutableStateOf(selectedSection.group == AdminNavGroup.MORE_STUDIO)
    }

    val surfaceBrush = if (!isDark) {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF6F0FF), Color(0xFFFDF2F8)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF160D31), Color(0xFF1E1142), Color(0xFF120928)))
    }
    val textColor = if (!isDark) Color(0xFF1A0F30) else Color.White
    val sectionLabelColor = if (!isDark) Color(0xFF7C3AED) else Color(0xFFCEBCFC)
    val shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)

    Column(
        modifier = modifier
            .width(270.dp)
            .fillMaxHeight()
            .shadow(16.dp, shape)
            .clip(shape)
            .background(surfaceBrush)
            .border(1.dp, if (!isDark) Color(0xFFDFD3F6) else Color(0x33FFFFFF), shape)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Brand Logo Header (Matches admin-overview.html lines 40-70)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0x66EC4899))
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0x44EC4899), Color(0x44A855F7), Color(0x33FBBF24))
                            )
                        )
                        .border(1.dp, Color(0x66F472B6), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "WishMagic Wand",
                        tint = Color(0xFFF472B6),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = textColor, fontWeight = FontWeight.ExtraBold)) {
                                append("Wish")
                            }
                            withStyle(
                                SpanStyle(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFFFF2A85), Color(0xFFEC4899), Color(0xFFF59E0B))
                                    ),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append("Magic")
                            }
                        },
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Create. Personalize. Celebrate.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (!isDark) Color(0xFF5E4F7D) else Color(0xFFD8B4FE)
                    )
                }
            }

            if (onCloseDrawer != null) {
                CompactIconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isDark) Color(0xFFF2EBFC) else Color(0x22FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Sidebar",
                        tint = textColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = if (!isDark) Color(0xFFEDE4FA) else Color(0x22FFFFFF))

        val primaryGroups = listOf(
            AdminNavGroup.MAIN,
            AdminNavGroup.CONTENT,
            AdminNavGroup.WISHES,
            AdminNavGroup.USERS,
            AdminNavGroup.SYSTEM
        )
        val moreSections = AdminSection.values().filter { it.group == AdminNavGroup.MORE_STUDIO }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            primaryGroups.forEach { group ->
                val sections = AdminSection.values().filter { it.group == group }
                if (group != AdminNavGroup.MAIN) {
                    item(key = "hdr_${group.name}") {
                        Text(
                            text = group.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = sectionLabelColor,
                            letterSpacing = 0.9.sp,
                            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 3.dp)
                        )
                    }
                }

                items(sections, key = { it.key }) { sec ->
                    val isSelected = sec == selectedSection
                    val badgeText = when (sec) {
                        AdminSection.TEMPLATES -> "$templatesCount"
                        AdminSection.WISHES -> "$wishesCount"
                        AdminSection.USERS -> "$usersCount"
                        AdminSection.ORDERS -> "$ordersCount"
                        AdminSection.FESTIVALS -> if (hasActiveFestival) "LIVE" else "🪔"
                        else -> sec.badgeDefault
                    }

                    SidebarNavRow(
                        section = sec,
                        isSelected = isSelected,
                        badgeText = badgeText,
                        isDark = isDark,
                        onClick = {
                            onSelectSection(sec)
                            onCloseDrawer?.invoke()
                        }
                    )
                }
            }

            // Expandable "More Studio Sections" (Matches admin-overview.html lines 160-201)
            item(key = "more_studio_toggle") {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isDark) Color(0x148B5CF6) else Color(0x1AFFFFFF))
                        .clickable { moreExpanded = !moreExpanded }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "MORE STUDIO SECTIONS (${moreSections.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = sectionLabelColor,
                        letterSpacing = 0.6.sp
                    )
                    Icon(
                        imageVector = if (moreExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = sectionLabelColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (moreExpanded) {
                items(moreSections, key = { it.key }) { sec ->
                    SidebarNavRow(
                        section = sec,
                        isSelected = sec == selectedSection,
                        badgeText = sec.badgeDefault,
                        isDark = isDark,
                        onClick = {
                            onSelectSection(sec)
                            onCloseDrawer?.invoke()
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = if (!isDark) Color(0xFFEDE4FA) else Color(0x22FFFFFF))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            GradientPillButton(
                text = "View Live Website",
                icon = Icons.Default.OpenInNew,
                onClick = onOpenLiveWebsite,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SidebarNavRow(
    section: AdminSection,
    isSelected: Boolean,
    badgeText: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    // Active item uses the exact Glowing Pink-Magenta-Purple Pill from admin-theme.css!
    val pillShape = RoundedCornerShape(999.dp)
    val textColor = if (isSelected) {
        Color.White
    } else {
        if (!isDark) Color(0xFF2B194D) else Color(0xDBEEE6FF)
    }
    val iconColor = if (isSelected) {
        Color.White
    } else {
        if (!isDark) Color(0xFF6D28D9) else Color(0xFFDAD0FF)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(6.dp, pillShape, spotColor = Color(0x77EC4899))
                        .clip(pillShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF2A85), Color(0xFFEC4899), Color(0xFFA855F7))
                            )
                        )
                        .border(1.dp, Color(0x55FFFFFF), pillShape)
                } else {
                    Modifier.clip(pillShape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = resolveSectionIcon(section),
                    contentDescription = section.shortLabel,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = section.shortLabel,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1
                )
            }

            if (badgeText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.24f)
                            else ClayColors.Primary.copy(alpha = 0.14f)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else ClayColors.Primary
                    )
                }
            }
        }
    }
}
