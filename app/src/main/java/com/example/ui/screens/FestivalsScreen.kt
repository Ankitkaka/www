package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FestivalItem
import com.example.ui.AdminViewModel
import com.example.ui.clay.*

/**
 * Website-Matched Festival Scheduler & Main Website Campaign Studio (`admin-festivals.html` & UTP v1.4.0 Section 7 1:1):
 * - A. Authoritative Publication Window (`F - 15 days` to `F + 3 days` inclusive, `F + 4` blocked in IST)
 * - B. 3-Tier Date Precedence (Tier-1 Year-Scoped Manual Date Override + Clear Override)
 * - C. Campaign Template & Relationship Variant Assignments (Two-Layer Variant Filtering: `allowedVariants`)
 * - D. Campaign Status Controls (`ACTIVE`, `SCHEDULED`, `PAUSED`, `ARCHIVED`)
 * - E. 1-Click Main Website Launch (`POST /api/admin/festivals/quick-launch` + `/api/admin/festivals/apply-storefront-theme`)
 */
@Composable
fun FestivalsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val festivals by viewModel.festivals.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val activeSpotlight by viewModel.activeFestivalSpotlight.collectAsState()

    var editingFestival by remember { mutableStateOf<FestivalItem?>(null) }
    var assigningFestival by remember { mutableStateOf<FestivalItem?>(null) }
    var showAddFestivalDialog by remember { mutableStateOf(false) }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "UTP v1.4.0 Server Festival Scheduler • IST ✨",
                titlePrefix = "Festival",
                pinkAccentWord = "Scheduler",
                titleSuffix = "& Spotlight",
                subtitle = "Authoritative F - 15 to F + 3 calendar-day publication window, Two-Layer Variant Assignments, and 1-Click Main Website Launch.",
                primaryButtonText = "🚀 Launch Diwali Spotlight",
                primaryButtonIcon = Icons.Default.RocketLaunch,
                onPrimaryClick = {
                    val diwali = festivals.firstOrNull { it.slug.contains("diwali", true) } ?: festivals.firstOrNull()
                    if (diwali != null) {
                        viewModel.quickLaunchFestival(diwali.id, diwali.name, diwali.badgeLabel, diwali.daysBefore, diwali.daysAfter)
                    } else {
                        viewModel.quickLaunchFestival("fest_diwali", "Diwali — Festival of Lights", "✨ Diwali Special", 15, 3)
                    }
                },
                secondaryButtonText = "+ Add Festival",
                secondaryButtonIcon = Icons.Default.AddCircleOutline,
                onSecondaryClick = { showAddFestivalDialog = true }
            )
        }

        // Main Website Storefront Theme & Panchang Calendar Sync Bar
        item {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink, ClayColors.Primary)),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Main Website Storefront Theme & Panchang Sync",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                    Text(
                        text = "Switch the live website between 3D Diwali Palace Mode and Default Cloudy Mode, or sync verified Panchang dates.",
                        fontSize = 11.sp,
                        color = mutedColor
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClayButton(
                            onClick = { viewModel.applyStorefrontFestivalTheme(true) },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("🪔 Apply Diwali 3D Theme", fontSize = 11.sp)
                        }
                        ClayButton(
                            onClick = { viewModel.applyStorefrontFestivalTheme(false) },
                            variant = ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("☁️ Default Cloudy Theme", fontSize = 11.sp)
                        }
                        ClayButton(
                            onClick = { viewModel.syncFestivalCalendarProvider() },
                            variant = ClayButtonVariant.EMERALD,
                            isDark = isDark
                        ) {
                            Icon(Icons.Default.EventRepeat, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sync Panchang Calendar", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Active Storefront Spotlight Card
        activeSpotlight?.let { spot ->
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Amber, ClayColors.Pink)),
                    isDark = isDark
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadgePill(text = "LIVE ON MAIN WEBSITE", isSuccess = true, isDark = isDark)
                            ClayPill(text = spot.badgeLabel, color = ClayColors.Pink, isDark = isDark)
                        }
                        Text(
                            text = "🪔 ${spot.name}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "Effective Date (F): ${spot.effectiveDate} • Active Window: F-${spot.daysBefore} to F+${spot.daysAfter} days (IST) • Immediate /w/:slug Unlock",
                            fontSize = 11.5.sp,
                            color = mutedColor
                        )
                    }
                }
            }
        }

        items(festivals, key = { it.id }) { fest ->
            TopAccentCard(
                accentBrush = if (fest.isActive) {
                    Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary))
                } else {
                    Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink))
                },
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StatusBadgePill(
                                    text = if (fest.isActive) "WINDOW OPEN (LIVE)" else fest.campaignStatus.uppercase(),
                                    isSuccess = fest.isActive,
                                    isDark = isDark
                                )
                                ClayPill(text = fest.badgeLabel, color = ClayColors.Pink, isDark = isDark)
                                if (fest.hasManualOverride) {
                                    ClayPill(text = "TIER-1 OVERRIDE", color = ClayColors.Amber, isDark = isDark)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = fest.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "Effective Date (F): ${fest.effectiveDate} • Window: F-${fest.daysBefore} to F+${fest.daysAfter} days (Blocked on F+${fest.daysAfter + 1})",
                                fontSize = 11.sp,
                                color = mutedColor
                            )
                            Text(
                                text = "Campaign Variants (${fest.allowedVariants.size}): ${fest.allowedVariants.joinToString(", ")} • Assigned Templates: ${fest.assignedTemplatesCount}",
                                fontSize = 10.5.sp,
                                color = mutedColor
                            )
                        }
                    }

                    // Campaign Status Quick Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status:", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = mutedColor)
                        listOf("ACTIVE", "SCHEDULED", "PAUSED").forEach { st ->
                            val isSelected = (st == "ACTIVE" && fest.isActive) || (!fest.isActive && fest.campaignStatus.equals(st, true))
                            ClayButton(
                                onClick = { viewModel.updateFestivalCampaignStatus(fest.id, st) },
                                variant = if (isSelected) ClayButtonVariant.EMERALD else ClayButtonVariant.SECONDARY,
                                isDark = isDark
                            ) {
                                Text(st, fontSize = 9.5.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClayButton(
                            onClick = {
                                viewModel.quickLaunchFestival(
                                    fest.id,
                                    fest.name,
                                    fest.badgeLabel,
                                    fest.daysBefore,
                                    fest.daysAfter
                                )
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (fest.isActive) "Sync" else "Launch", fontSize = 10.5.sp)
                        }

                        CompactIconButton(
                            onClick = { assigningFestival = fest },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ClayColors.Primary.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = "Assign Templates", tint = ClayColors.Primary, modifier = Modifier.size(16.dp))
                        }

                        CompactIconButton(
                            onClick = { editingFestival = fest },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ClayColors.Amber.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Edit Date", tint = ClayColors.Amber, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal 1: Campaign Template & Relationship Variant Assignments (`admin-festivals.html` Section C!)
    assigningFestival?.let { fest ->
        var selectedTemplateId by remember(fest, templates) {
            mutableStateOf(templates.firstOrNull()?.id ?: "royal-diwali-lights")
        }
        var allowedVariants by remember(fest) { mutableStateOf(fest.allowedVariants) }
        var badgeLabel by remember(fest) { mutableStateOf(fest.badgeLabel) }

        val selectedTplObj = templates.firstOrNull { it.id == selectedTemplateId }
        val candidateVariants = remember(selectedTplObj, fest) {
            val tplVars = if (selectedTplObj != null) {
                (selectedTplObj.variantsList.map { it.relationship } + selectedTplObj.enabledVariants)
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() }
                    .distinct()
            } else emptyList()
            if (tplVars.isNotEmpty()) tplVars else fest.allowedVariants
        }

        Dialog(onDismissRequest = { assigningFestival = null }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
                isDark = isDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Campaign Template & Variant Assignments",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "Festival: ${fest.name} • Two-Layer Variant Visibility Enforcement",
                                fontSize = 10.5.sp,
                                color = mutedColor
                            )
                        }
                        CompactIconButton(onClick = { assigningFestival = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedColor)
                        }
                    }

                    Text("1. SELECT TEMPLATE TO ASSIGN TO CAMPAIGN:", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = mutedColor)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        templates.forEach { tpl ->
                            val isSelected = selectedTemplateId == tpl.id
                            val isAssigned = fest.assignedTemplateIds.contains(tpl.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ClayColors.Pink.copy(alpha = 0.14f) else ClayColors.Primary.copy(alpha = 0.06f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tpl.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Text("ID: ${tpl.id} • Variants: ${tpl.enabledVariants.joinToString(", ")}", fontSize = 10.sp, color = mutedColor)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ClayButton(
                                        onClick = {
                                            selectedTemplateId = tpl.id
                                            allowedVariants = tpl.enabledVariants
                                        },
                                        variant = if (isSelected) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                        isDark = isDark
                                    ) {
                                        Text(if (isSelected) "Selected" else "Select", fontSize = 10.sp)
                                    }
                                    if (isAssigned) {
                                        CompactIconButton(
                                            onClick = { viewModel.removeTemplateFromFestivalCampaign(fest.id, tpl.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = ClayColors.Rose)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "2. ALLOWED RELATIONSHIP VARIANTS FOR THIS FESTIVAL CAMPAIGN (${allowedVariants.size}):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = mutedColor
                    )
                    Text(
                        text = "e.g. Restrict Bhai Dooj / Raksha Bandhan to [sister, brother] without editing the template ZIP!",
                        fontSize = 10.sp,
                        color = mutedColor
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        candidateVariants.forEach { vSlug ->
                            val enabled = allowedVariants.contains(vSlug)
                            ClayButton(
                                onClick = {
                                    allowedVariants = if (enabled && allowedVariants.size > 1) {
                                        allowedVariants - vSlug
                                    } else if (!enabled) {
                                        allowedVariants + vSlug
                                    } else allowedVariants
                                },
                                variant = if (enabled) ClayButtonVariant.EMERALD else ClayButtonVariant.SECONDARY,
                                isDark = isDark
                            ) {
                                Text(if (enabled) "☑ $vSlug" else "☐ $vSlug", fontSize = 10.5.sp)
                            }
                        }
                    }

                    ClayTextField(
                        value = badgeLabel,
                        onValueChange = { badgeLabel = it },
                        label = "CAMPAIGN TEMPLATE CARD BADGE RIBBON",
                        placeholder = "✨ Diwali Special",
                        isDark = isDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { assigningFestival = null }) {
                            Text("Close", color = mutedColor)
                        }
                        Spacer(Modifier.width(8.dp))
                        ClayButton(
                            onClick = {
                                viewModel.assignTemplateToFestivalCampaign(
                                    festivalId = fest.id,
                                    templateId = selectedTemplateId,
                                    allowedVariants = allowedVariants,
                                    badgeLabel = badgeLabel
                                )
                                assigningFestival = null
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save Assignment to D1", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal 2: Tier-1 Manual Date Override + Clear Override
    editingFestival?.let { fest ->
        var newDate by remember(fest) { mutableStateOf(fest.effectiveDate) }
        var daysBefore by remember(fest) { mutableStateOf(fest.daysBefore.toString()) }
        var daysAfter by remember(fest) { mutableStateOf(fest.daysAfter.toString()) }

        Dialog(onDismissRequest = { editingFestival = null }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Tier-1 Manual Date Override",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = "Festival: ${fest.name} • Window: F - $daysBefore to F + $daysAfter days (IST)",
                        fontSize = 11.5.sp,
                        color = mutedColor
                    )
                    ClayTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = "Effective Festival Date (YYYY-MM-DD)",
                        placeholder = "2026-11-08",
                        isDark = isDark
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = daysBefore,
                                onValueChange = { daysBefore = it },
                                label = "Days Before (F - X)",
                                isDark = isDark
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = daysAfter,
                                onValueChange = { daysAfter = it },
                                label = "Days After (F + Y)",
                                isDark = isDark
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.removeFestivalOverride(fest.id, fest.name)
                                editingFestival = null
                            }
                        ) {
                            Text("Clear Override", color = ClayColors.Rose, fontSize = 11.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { editingFestival = null }) {
                                Text("Cancel", color = mutedColor)
                            }
                            ClayButton(
                                onClick = {
                                    viewModel.saveFestivalOverride(
                                        fest.id,
                                        fest.name,
                                        newDate,
                                        daysBefore.toIntOrNull() ?: 15,
                                        daysAfter.toIntOrNull() ?: 3
                                    )
                                    editingFestival = null
                                },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark
                            ) {
                                Text("Save Override", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddFestivalDialog) {
        var name by remember { mutableStateOf("") }
        var slug by remember { mutableStateOf("") }
        var effectiveDate by remember { mutableStateOf("2026-11-08") }
        var badgeLabel by remember { mutableStateOf("✨ Festival Special") }
        var daysBefore by remember { mutableStateOf("15") }
        var daysAfter by remember { mutableStateOf("3") }

        Dialog(onDismissRequest = { showAddFestivalDialog = false }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Register Festival Campaign in D1",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    ClayTextField(value = name, onValueChange = { name = it }, label = "Festival Name", placeholder = "Bhai Dooj", isDark = isDark)
                    ClayTextField(value = slug, onValueChange = { slug = it }, label = "Festival Slug", placeholder = "bhai-dooj", isDark = isDark)
                    ClayTextField(value = effectiveDate, onValueChange = { effectiveDate = it }, label = "Effective Date (YYYY-MM-DD)", placeholder = "2026-11-10", isDark = isDark)
                    ClayTextField(value = badgeLabel, onValueChange = { badgeLabel = it }, label = "Spotlight Badge Label", placeholder = "✨ Bhai Dooj Special", isDark = isDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(value = daysBefore, onValueChange = { daysBefore = it }, label = "Days Before", isDark = isDark)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(value = daysAfter, onValueChange = { daysAfter = it }, label = "Days After", isDark = isDark)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddFestivalDialog = false }) { Text("Cancel", color = mutedColor) }
                        Spacer(Modifier.width(8.dp))
                        ClayButton(
                            onClick = {
                                if (name.isNotBlank()) {
                                    viewModel.createNewFestival(
                                        name = name,
                                        slug = slug,
                                        effectiveDate = effectiveDate,
                                        badgeLabel = badgeLabel,
                                        daysBefore = daysBefore.toIntOrNull() ?: 15,
                                        daysAfter = daysAfter.toIntOrNull() ?: 3
                                    )
                                    showAddFestivalDialog = false
                                }
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Text("Create Festival", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
