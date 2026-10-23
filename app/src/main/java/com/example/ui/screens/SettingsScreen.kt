package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminViewModel
import com.example.ui.clay.*
import kotlin.math.roundToInt

/**
 * Website-Matched Settings & Storefront CMS Screen (Matches `24632.jpg` & `admin-settings.html` 1:1):
 * - Cosmic Hero Banner: `Main Website Live CMS & Control Matrix ✨`, `Settings & Storefront CMS`,
 *   `Publish to Live Site`, `Reset Defaults`, `View Main Website`
 * - Pill Category Selector:
 *   `🏪 1. Storefront & Support`, `🔗 2. Domain & Wish Links`, `🏷️ 3. Pricing & Free Offers`,
 *   `🛡️ 4. Team, Security & Backup`, `📋 Show All Settings`
 * - Storefront Hero Section Manager (`● D1 Persistence Active`, `Save Hero Settings`, `HERO PILL BADGE TEXT`, `HERO HEADLINE`)
 * - 3D Diwali Hero vs Photo Mode + 4-View Diwali Palace Background + 50-Level Blur Slider (`1..50`)
 * - Central Link Manager (`Migrate All Links`) & Wish Lifecycle Expiry
 */
@Composable
fun SettingsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val settings by viewModel.siteSettings.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    var siteName by remember(settings) { mutableStateOf(settings.siteName) }
    var siteTagline by remember(settings) { mutableStateOf(settings.siteTagline) }
    var heroBadgeText by remember(settings) { mutableStateOf(settings.heroBadgeText) }
    var heroHeadline by remember(settings) { mutableStateOf(settings.heroHeadline) }
    var supportEmail by remember(settings) { mutableStateOf(settings.supportEmail) }
    var heroDisplayMode by remember(settings) { mutableStateOf(settings.heroDisplayMode) }
    var siteBackgroundMode by remember(settings) { mutableStateOf(settings.siteBackgroundMode) }
    var siteBackgroundBlur by remember(settings) { mutableStateOf(settings.siteBackgroundBlur.toFloat()) }
    var bannerText by remember(settings) { mutableStateOf(settings.announcementBannerText) }
    var bannerEnabled by remember(settings) { mutableStateOf(settings.announcementBannerEnabled) }
    var canonicalUrl by remember(settings) { mutableStateOf(settings.canonicalBaseUrl) }
    var maintenanceMode by remember(settings) { mutableStateOf(settings.maintenanceMode) }
    var freeExpiryDays by remember(settings) { mutableStateOf(settings.defaultFreeExpiryDays.toString()) }
    var paidExpiryDays by remember(settings) { mutableStateOf(settings.defaultPaidExpiryDays.toString()) }

    var selectedGroup by remember { mutableStateOf(0) }
    val categoryGroups = listOf(
        "🏪 1. Storefront & Support",
        "🔗 2. Domain & Wish Links",
        "🏷️ 3. Pricing & Free Offers",
        "🛡️ 4. Team, Security & Backup",
        "📋 Show All Settings"
    )

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    fun publishAllNow(customLabel: String? = null) {
        viewModel.updateSiteSettings(
            heroDisplayMode = heroDisplayMode,
            siteBackgroundMode = siteBackgroundMode,
            siteBackgroundBlur = siteBackgroundBlur.roundToInt().coerceIn(1, 50),
            bannerText = bannerText,
            bannerEnabled = bannerEnabled,
            canonicalUrl = canonicalUrl,
            siteName = siteName,
            siteTagline = siteTagline,
            heroBadgeText = heroBadgeText,
            heroHeadline = heroHeadline,
            supportEmail = supportEmail,
            maintenanceMode = maintenanceMode,
            defaultFreeExpiryDays = freeExpiryDays.toIntOrNull() ?: 7,
            defaultPaidExpiryDays = paidExpiryDays.toIntOrNull() ?: 365,
            label = customLabel ?: "Published live CMS settings to ${serverUrl.ifBlank { "https://wishmagic.pages.dev" }}!"
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        // 1. COSMIC HERO BANNER (Exact match to screenshot 24632.jpg!)
        item {
            CosmicHeroBanner(
                welcomePillText = "Main Website Live CMS & Control Matrix ✨",
                titlePrefix = "Settings &",
                pinkAccentWord = "Storefront",
                titleSuffix = "CMS",
                subtitle = "Configure live main website hero text & visuals, storefront sections, active templates, team roles, and global platform parameters.",
                primaryButtonText = "Publish to Live Site",
                primaryButtonIcon = Icons.Default.Publish,
                onPrimaryClick = { publishAllNow("Published Storefront CMS to Live Website!") },
                secondaryButtonText = "Reset Defaults",
                secondaryButtonIcon = Icons.Default.Refresh,
                onSecondaryClick = { viewModel.resetSiteSettings() },
                tertiaryButtonText = "↗ View Main Website",
                onTertiaryClick = {
                    val url = serverUrl.ifBlank { "https://wishmagic.pages.dev" }
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                }
            )
        }

        // 2. COMPACT HORIZONTAL CATEGORY TABS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categoryGroups.forEachIndexed { idx, title ->
                    ClayButton(
                        onClick = { selectedGroup = idx },
                        variant = if (selectedGroup == idx) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Text(title, fontSize = 10.5.sp, maxLines = 1)
                    }
                }
            }
        }

        // GROUP 0 OR 4: STOREFRONT HERO SECTION MANAGER + 3D SCENE & 50-LEVEL BLUR (Exact match to 24632.jpg!)
        if (selectedGroup == 0 || selectedGroup == 4) {
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)),
                    isDark = isDark
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ClayColors.Primary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewCarousel,
                                    contentDescription = null,
                                    tint = ClayColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Storefront Hero Section Manager",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                                Text(
                                    text = "Update the headline, description, floating centerpiece visual, and CTA buttons on the homepage.",
                                    fontSize = 11.5.sp,
                                    color = mutedColor
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusBadgePill(
                                text = "D1 Persistence Active",
                                isSuccess = true,
                                isDark = isDark
                            )
                            GradientPillButton(
                                text = "Save Hero Settings",
                                icon = Icons.Default.Save,
                                onClick = { publishAllNow("Hero badge, headline & 3D visuals saved to D1!") },
                                brush = Brush.horizontalGradient(listOf(Color(0xFFFF2A85), Color(0xFF9333EA)))
                            )
                        }

                        Divider(color = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder)

                        ClayTextField(
                            value = heroBadgeText,
                            onValueChange = { heroBadgeText = it },
                            label = "HERO PILL BADGE TEXT",
                            placeholder = "Make Every Moment Magical ✨",
                            isDark = isDark
                        )

                        ClayTextField(
                            value = heroHeadline,
                            onValueChange = { heroHeadline = it },
                            label = "HERO HEADLINE TITLE",
                            placeholder = "Gift a Magical Web-Experience!",
                            isDark = isDark
                        )

                        ClayTextField(
                            value = siteTagline,
                            onValueChange = { siteTagline = it },
                            label = "HERO SUBHEADLINE / TAGLINE",
                            placeholder = "Turn birthdays, anniversaries & festivals into interactive 3D celebration websites.",
                            isDark = isDark
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                ClayTextField(
                                    value = siteName,
                                    onValueChange = { siteName = it },
                                    label = "BRAND NAME",
                                    isDark = isDark
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ClayTextField(
                                    value = supportEmail,
                                    onValueChange = { supportEmail = it },
                                    label = "SUPPORT EMAIL",
                                    isDark = isDark
                                )
                            }
                        }
                    }
                }
            }

            // 3D Hero Scene + 4-View Background + 50-Level Blur Studio
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink, ClayColors.Primary)),
                    isDark = isDark
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Text(
                            text = "3D Hero Scene & 4-View Page Background Blur (1–50)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Text(
                            text = "Controls the live homepage 3D Diwali Scene vs Photo Hero and the 4-view background blur level.",
                            fontSize = 11.sp,
                            color = mutedColor
                        )

                        Text("1. HERO CENTERPIECE DISPLAY MODE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = mutedColor)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "diwali-3d" to "🪔 3D Diwali Scene",
                                "image" to "🖼️ Custom Photo Hero"
                            ).forEach { (mode, label) ->
                                ClayButton(
                                    onClick = {
                                        heroDisplayMode = mode
                                        publishAllNow("Switched Homepage Hero to $label!")
                                    },
                                    variant = if (heroDisplayMode == mode) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 11.sp)
                                }
                            }
                        }

                        Text("2. 4-VIEW WEBSITE BACKGROUND THEME", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = mutedColor)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "diwali-palace" to "🏮 Diwali Palace (4-View)",
                                "cloudy" to "☁️ Cosmic Clouds"
                            ).forEach { (bgMode, label) ->
                                ClayButton(
                                    onClick = {
                                        siteBackgroundMode = bgMode
                                        publishAllNow("Switched 4-View Website Background to $label!")
                                    },
                                    variant = if (siteBackgroundMode == bgMode) ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 11.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("3. BACKGROUND-ONLY BLUR INTENSITY", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = mutedColor)
                            ClayPill(
                                text = "Level ${siteBackgroundBlur.roundToInt()} / 50",
                                color = ClayColors.Pink,
                                isDark = isDark
                            )
                        }
                        Slider(
                            value = siteBackgroundBlur,
                            onValueChange = { siteBackgroundBlur = it },
                            onValueChangeFinished = {
                                publishAllNow("Updated 4-View Background Blur to Level ${siteBackgroundBlur.roundToInt()}/50!")
                            },
                            valueRange = 1f..50f,
                            steps = 48
                        )
                    }
                }
            }
        }

        // GROUP 1 OR 4: CENTRAL LINK MANAGER & WISH LIFECYCLE
        if (selectedGroup == 1 || selectedGroup == 4) {
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Sky, ClayColors.Primary)),
                    isDark = isDark
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Central Link Manager (UTP v1.4.0 Logical Links)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Text(
                            text = "All template `global.*` and `template.*` links resolve against this canonical production domain.",
                            fontSize = 11.sp,
                            color = mutedColor
                        )
                        ClayTextField(
                            value = canonicalUrl,
                            onValueChange = { canonicalUrl = it },
                            label = "CANONICAL PRODUCTION DOMAIN",
                            placeholder = "https://wishmagic.pages.dev",
                            isDark = isDark
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClayButton(
                                onClick = { publishAllNow("Updated Canonical Production Domain!") },
                                variant = ClayButtonVariant.PRIMARY,
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Domain", fontSize = 11.sp)
                            }
                            ClayButton(
                                onClick = { viewModel.migrateTemplateLinks(canonicalUrl) },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Migrate All Links", fontSize = 11.sp)
                            }
                        }

                        ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text("1. Global WishMagic Logical Links (global.*):", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                                settings.globalRoutes.forEach { (k, v) ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(k, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = ClayColors.Primary)
                                        Text("${canonicalUrl.removeSuffix("/")}$v", fontSize = 10.sp, color = mutedColor)
                                    }
                                }
                               Spacer(Modifier.height(4.dp))
                                Text("2. Template-Specific Creation Links (template.* — Preserves template, variant, rel & campaign):", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                                settings.templateActionPatterns.forEach { (k, v) ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(k, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = ClayColors.Pink)
                                        Text(v, fontSize = 9.5.sp, color = mutedColor)
                                    }
                                }
                            }
                        }

                        Divider(color = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder)

                        Text(
                            text = "Wish Link Lifecycle & Expiry Windows",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                ClayTextField(
                                    value = freeExpiryDays,
                                    onValueChange = { freeExpiryDays = it },
                                    label = "Free Wish Expiry (Days)",
                                    isDark = isDark
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ClayTextField(
                                    value = paidExpiryDays,
                                    onValueChange = { paidExpiryDays = it },
                                    label = "Paid/VIP Expiry (Days)",
                                    isDark = isDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // GROUP 2 OR 4: PROMO BANNER & OFFERS
        if (selectedGroup == 2 || selectedGroup == 4) {
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink)),
                    isDark = isDark
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Storefront Top Promo Banner",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor
                                )
                                Text(
                                    text = "Displays live announcement ribbon at the top of `index.html`",
                                    fontSize = 11.sp,
                                    color = mutedColor
                                )
                            }
                            ClaySwitch(
                                checked = bannerEnabled,
                                onCheckedChange = {
                                    bannerEnabled = it
                                    publishAllNow("Promo banner visibility updated!")
                                },
                                isDark = isDark
                            )
                        }
                        ClayTextField(
                            value = bannerText,
                            onValueChange = { bannerText = it },
                            label = "PROMO BANNER TEXT",
                            isDark = isDark
                        )
                    }
                }
            }
        }

        // GROUP 3 OR 4: MAINTENANCE & SECURITY
        if (selectedGroup == 3 || selectedGroup == 4) {
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary)),
                    isDark = isDark
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Storefront Maintenance Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor
                                )
                                Text(
                                    text = "Pause public wish creation while Admin Studio stays online",
                                    fontSize = 11.sp,
                                    color = mutedColor
                                )
                            }
                            ClaySwitch(
                                checked = maintenanceMode,
                                onCheckedChange = {
                                    maintenanceMode = it
                                    publishAllNow("Maintenance mode set to $it!")
                                },
                                isDark = isDark
                            )
                        }
                    }
                }
            }
        }

        item {
            GradientPillButton(
                text = "Publish All CMS Changes to Live Website",
                icon = Icons.Default.RocketLaunch,
                onClick = { publishAllNow("All CMS settings published to Cloudflare D1!") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
