package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.AdminViewModel
import com.example.ui.clay.*
import com.example.ui.components.FullscreenTemplatePreviewDialog
import com.example.ui.components.TemplateLivePreviewBox

private fun copyTextToClipboard(context: Context, label: String, text: String) {
    runCatching {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}

private const val UTP_V140_MASTER_PROMPT = """[COPY START]
You are a senior front-end creative engineer building a self-contained, interactive HTML/CSS/JS celebration or festival template package for the "WishMagic" platform following the WishMagic Universal Template Protocol (UTP v1.4.0).
Follow all 12 rules:
1. Flat root ZIP structure: wishmagic.json (schemaVersion: "1.4.0", compatibility.runtimeApi: "1.4"), index.html (<html lang="en" data-wm-protocol="1.4.0">), css/style.css (:root --wm-primary, --wm-secondary, --wm-accent, --wm-bg, --wm-text), js/template.js, assets/images/..., assets/audio/...
2. Three-way agreement for every field (wishmagic.json fields[] <-> index.html [data-wm-field][data-wm-type] <-> runtime textContent hydration).
3. 3-level hierarchical sections & subsections (data-wm-section, data-wm-subsection, emptyParentPolicy: "collapse" | "keep"). Guard all DOM queries with null checks.
4. Wrap multi-photo cards in data-wm-photo-slot="photo_X" inside auto-reflowing CSS Grid/Flexbox.
5. Logical links in actions[]: use type "template_link" (logicalLink: "template.customize" | "template.create") and "global_link" (logicalLink: "global.explore" | "global.browseTemplates" | "global.home"). Never hardcode domain URLs.
6. Multi-relationship variants[]: tailored theme, messages, visibilityOverrides, and actions per relationship (family, sister, brother, best_friend, boyfriend, girlfriend, etc.).
7. Optional festivalMetadata: { festivalTags, suitableRelationships, campaignCompatibility: "evergreen_and_festival", displayBadge, suggestedPublicationPreferences: { suggestedDaysBefore: 15, suggestedDaysAfter: 3, advisoryOnly: true } }. NEVER hardcode authoritativeFestivalDate, forceActive, or bypassSchedule.
8. Include <script src="/js/wishmagic-template-protocol.js"></script> before js/template.js and listen for WISHMAGIC_INIT, WISHMAGIC_LIVE_UPDATE, WISHMAGIC_UPDATE, and wishmagic:data-updated.
[COPY END]"""

@Composable
fun AddTemplateScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val pendingZip by viewModel.pendingZipPackage.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var studioTab by remember { mutableStateOf(0) }
    var showFullscreenPreview by remember { mutableStateOf(false) }

    var updateMode by remember { mutableStateOf("full") }
    var decisionMode by remember(pendingZip) {
        mutableStateOf(if (pendingZip?.matchedExistingTemplateId?.isNotBlank() == true) "update_existing" else "create_new")
    }
    var customName by remember(pendingZip) { mutableStateOf(pendingZip?.suggestedName ?: "") }
    var customCategory by remember(pendingZip) { mutableStateOf(pendingZip?.detectedCategory?.replaceFirstChar { it.uppercase() } ?: "Birthday") }
    var customDescription by remember(pendingZip) { mutableStateOf(pendingZip?.detectedDescription ?: "") }
    var customPrice by remember(pendingZip) { mutableStateOf((pendingZip?.detectedPrice?.toInt() ?: 0).toString()) }
    var customCoverUrl by remember(pendingZip) { mutableStateOf(pendingZip?.extractedCoverUri ?: "") }
    var enabledVariants by remember(pendingZip) {
        mutableStateOf(pendingZip?.detectedVariants ?: emptyList())
    }
    var configuredVariantsList by remember(pendingZip) {
        mutableStateOf(pendingZip?.variantsList ?: emptyList())
    }
    var configuredSections by remember(pendingZip) {
        mutableStateOf(pendingZip?.sections ?: emptyList())
    }
    var configuredActions by remember(pendingZip) {
        mutableStateOf(pendingZip?.actions ?: emptyList())
    }
    var configuredCapabilities by remember(pendingZip) {
        mutableStateOf(pendingZip?.capabilities ?: emptyList())
    }
    var festivalTagsCsv by remember(pendingZip) {
        mutableStateOf(pendingZip?.festivalMetadata?.festivalTags?.joinToString(", ") ?: "diwali, deepavali, bhai-dooj")
    }
    var festivalBadge by remember(pendingZip) {
        mutableStateOf(pendingZip?.festivalMetadata?.displayBadge ?: "✨ Special Edition")
    }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    val zipPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            studioTab = 0
            viewModel.processIncomingZipUri(context, uri, showModalDialog = false)
        }
    }

    val coverImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadPendingZipCustomCover(context, uri) { uploadedUrl ->
                customCoverUrl = uploadedUrl
            }
        }
    }

    val allCandidateVariants = remember(pendingZip) {
        ((pendingZip?.variantsList?.map { it.relationship } ?: emptyList()) + (pendingZip?.detectedVariants ?: emptyList()))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    if (showFullscreenPreview && pendingZip != null) {
        val pkg = pendingZip!!
        FullscreenTemplatePreviewDialog(
            title = customName.ifBlank { pkg.suggestedName },
            stagedHtml = pkg.stagedPreviewHtml,
            baseUrl = pkg.stagedPreviewBaseUrl,
            variantsList = configuredVariantsList,
            actionsList = configuredActions,
            fieldsList = pkg.fields,
            sectionsList = configuredSections,
            isDark = isDark,
            onPublishNow = {
                showFullscreenPreview = false
                val fm = pkg.festivalMetadata.copy(
                    festivalTags = festivalTagsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    suitableRelationships = enabledVariants,
                    displayBadge = festivalBadge
                )
                viewModel.quickPublishPendingZip(
                    updateMode = updateMode,
                    customName = customName,
                    customPrice = customPrice.toDoubleOrNull() ?: 0.0,
                    enabledVariants = enabledVariants,
                    sections = configuredSections,
                    actions = configuredActions,
                    festivalMetadata = fm,
                    decisionMode = decisionMode,
                    customCoverUrlOverride = customCoverUrl,
                    categoryOverride = customCategory.lowercase(),
                    descriptionOverride = customDescription,
                    capabilitiesOverride = configuredCapabilities,
                    variantsListOverride = configuredVariantsList
                )
            },
            onDismiss = { showFullscreenPreview = false }
        )
    }

    val studioTabs = listOf(
        "📱 Live Preview",
        "🖼️ Cover & Info",
        "👥 Variants (${configuredVariantsList.size.coerceAtLeast(allCandidateVariants.size)})",
        "🧩 Sections & Fields",
        "☁️ Cloud & Audit"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "UTP v1.4.0 Live Upload Studio",
                titlePrefix = "Upload",
                pinkAccentWord = "Template",
                titleSuffix = "ZIP",
                primaryButtonText = if (pendingZip != null) "Change .ZIP" else "Choose .ZIP",
                primaryButtonIcon = Icons.Default.FolderZip,
                onPrimaryClick = { zipPicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                secondaryButtonText = if (pendingZip != null) "Full Preview" else "Web Studio",
                secondaryButtonIcon = if (pendingZip != null) Icons.Default.Fullscreen else Icons.Default.OpenInBrowser,
                onSecondaryClick = {
                    if (pendingZip != null) {
                        showFullscreenPreview = true
                    } else {
                        viewModel.openFullStudioFor(AdminSection.ADD_TEMPLATE)
                    }
                }
            )
        }

        if (pendingZip != null) {
            val pkg = pendingZip!!

            // 1. Staged Package Summary Header + Instant Publish Bar
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary, ClayColors.Pink)),
                    isDark = isDark,
                    contentPadding = PaddingValues(11.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ClayColors.Primary.copy(alpha = 0.15f))
                                        .border(1.dp, ClayColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .clickable { coverImagePicker.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (customCoverUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = customCoverUrl,
                                            contentDescription = customName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Add Cover",
                                            tint = ClayColors.Primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatusBadgePill(
                                            text = if (pkg.validationPassed) "UTP v${pkg.detectedSchemaVersion} READY" else "WARNINGS",
                                            isSuccess = pkg.validationPassed,
                                            isDark = isDark
                                        )
                                        ClayPill(text = "${pkg.sizeKb} KB", color = ClayColors.Primary, isDark = isDark)
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = customName.ifBlank { pkg.suggestedName },
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "/${pkg.detectedSlug} • ${customCategory} • ${if ((customPrice.toIntOrNull() ?: 0) > 0) "VIP ₹$customPrice" else "FREE"}",
                                        fontSize = 10.sp,
                                        color = mutedColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            CompactIconButton(
                                onClick = { viewModel.clearPendingZip() },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ClayColors.Rose.copy(alpha = 0.14f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear ZIP", tint = ClayColors.Rose, modifier = Modifier.size(15.dp))
                            }
                        }

                        // Cloud Routing Stat Pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            ClayPill(
                                text = "ImgBB: ${pkg.imagesCount} Imgs",
                                color = ClayColors.Amber,
                                isDark = isDark
                            )
                            ClayPill(
                                text = "B2: ${pkg.codeFilesCount} Code / ${pkg.audioCount} Audio",
                                color = ClayColors.Sky,
                                isDark = isDark
                            )
                            ClayPill(
                                text = "${configuredVariantsList.size.coerceAtLeast(allCandidateVariants.size)} Variants",
                                color = ClayColors.Primary,
                                isDark = isDark
                            )
                            ClayPill(
                                text = "${configuredSections.size} Sections • ${pkg.fields.size.coerceAtLeast(pkg.detectedFieldsCount)} Fields",
                                color = ClayColors.Emerald,
                                isDark = isDark
                            )
                        }

                        if (pkg.matchedExistingTemplateId.isNotBlank()) {
                            ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(8.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(
                                        text = "🔄 Matches Existing Template '${pkg.matchedExistingTemplateName}' (${pkg.matchedExistingTemplateId}):",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ClayColors.Emerald
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(
                                            "update_existing" to "🔄 Update Existing",
                                            "create_new" to "✨ Create New"
                                        ).forEach { (dKey, dLabel) ->
                                            ClayButton(
                                                onClick = { decisionMode = dKey },
                                                variant = if (decisionMode == dKey) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                                isDark = isDark,
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(dLabel, fontSize = 10.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Primary Publish & Mode Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ClayButton(
                                onClick = { updateMode = if (updateMode == "full") "patch" else "full" },
                                variant = ClayButtonVariant.SECONDARY,
                                isDark = isDark,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (updateMode == "full") "📦 Full ZIP" else "🩹 Diff Patch",
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                GradientPillButton(
                                    text = if (isSyncing) "⏳ Uploading to B2 & D1..." else "🚀 Publish Template Live Now",
                                    icon = Icons.Default.CloudUpload,
                                    onClick = {
                                        val fm = pkg.festivalMetadata.copy(
                                            festivalTags = festivalTagsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                            suitableRelationships = enabledVariants,
                                            displayBadge = festivalBadge
                                        )
                                        viewModel.quickPublishPendingZip(
                                            updateMode = updateMode,
                                            customName = customName,
                                            customPrice = customPrice.toDoubleOrNull() ?: 0.0,
                                            enabledVariants = enabledVariants,
                                            sections = configuredSections,
                                            actions = configuredActions,
                                            festivalMetadata = fm,
                                            decisionMode = decisionMode,
                                            customCoverUrlOverride = customCoverUrl,
                                            categoryOverride = customCategory.lowercase(),
                                            descriptionOverride = customDescription,
                                            capabilitiesOverride = configuredCapabilities,
                                            variantsListOverride = configuredVariantsList
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // 2. 5-Tab Studio Switcher Strip
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    studioTabs.forEachIndexed { idx, tabLabel ->
                        ClayButton(
                            onClick = { studioTab = idx },
                            variant = if (studioTab == idx) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                            isDark = isDark,
                            contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
                        ) {
                            Text(tabLabel, fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }
            }

            // 3. Active Studio Tab Content
            when (studioTab) {
                // TAB 0: INTERACTIVE LIVE SANDBOX PREVIEW
                0 -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink, ClayColors.Emerald)),
                            isDark = isDark,
                            contentPadding = PaddingValues(11.dp)
                        ) {
                            if (pkg.stagedPreviewHtml.isNotBlank()) {
                                TemplateLivePreviewBox(
                                    title = customName.ifBlank { pkg.suggestedName },
                                    stagedHtml = pkg.stagedPreviewHtml,
                                    baseUrl = pkg.stagedPreviewBaseUrl,
                                    variantsList = configuredVariantsList,
                                    actionsList = configuredActions,
                                    fieldsList = pkg.fields,
                                    sectionsList = configuredSections,
                                    isDark = isDark,
                                    previewHeight = 420.dp,
                                    showFieldTester = true,
                                    onOpenFullscreen = { showFullscreenPreview = true }
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "⚠️ No index.html found in this ZIP (Diff Patch Mode)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ClayColors.Amber
                                    )
                                    Text(
                                        text = "Switch to 'Mode 2: Diff Patch' to patch an existing template, or upload a full ZIP with index.html to see the live interactive preview.",
                                        fontSize = 10.5.sp,
                                        color = mutedColor
                                    )
                                }
                            }
                        }
                    }
                }

                // TAB 1: COVER IMAGE, IDENTITY, PRICING & CAPABILITIES
                1 -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "🖼️ Template Cover Image & Storefront Card",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(ClayColors.Primary.copy(alpha = 0.14f))
                                            .border(1.dp, ClayColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (customCoverUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = customCoverUrl,
                                                contentDescription = "Cover Preview",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = ClayColors.Primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ClayButton(
                                            onClick = { coverImagePicker.launch("image/*") },
                                            variant = ClayButtonVariant.PINK,
                                            isDark = isDark,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(5.dp))
                                            Text("Upload Cover from Phone", fontSize = 10.5.sp, maxLines = 1)
                                        }
                                        if (pkg.extractedCoverUri.isNotBlank() && customCoverUrl != pkg.extractedCoverUri) {
                                            ClayButton(
                                                onClick = { customCoverUrl = pkg.extractedCoverUri },
                                                variant = ClayButtonVariant.SECONDARY,
                                                isDark = isDark,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("↺ Restore ZIP Cover Image", fontSize = 10.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }

                                ClayTextField(
                                    value = if (customCoverUrl.startsWith("data:")) "Extracted from ZIP (${pkg.fileName})" else customCoverUrl,
                                    onValueChange = { if (!it.startsWith("Extracted")) customCoverUrl = it },
                                    label = "Or Paste Cover Image URL (ImgBB / HTTPS)",
                                    placeholder = "https://i.ibb.co/...",
                                    isDark = isDark
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = customName,
                                            onValueChange = { customName = it },
                                            label = "Template Display Name",
                                            isDark = isDark
                                        )
                                    }
                                    Box(modifier = Modifier.width(100.dp)) {
                                        ClayTextField(
                                            value = customPrice,
                                            onValueChange = { customPrice = it },
                                            label = "Price (₹)",
                                            isDark = isDark
                                        )
                                    }
                                }

                                // Quick Price Presets
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("0" to "FREE (₹0)", "49" to "VIP ₹49", "59" to "VIP ₹59", "99" to "VIP ₹99", "149" to "Royal ₹149").forEach { (pVal, pLbl) ->
                                        ClayButton(
                                            onClick = { customPrice = pVal },
                                            variant = if (customPrice == pVal) ClayButtonVariant.AMBER else ClayButtonVariant.SECONDARY,
                                            isDark = isDark,
                                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 5.dp)
                                        ) {
                                            Text(pLbl, fontSize = 9.5.sp, maxLines = 1)
                                        }
                                    }
                                }

                                Text("STOREFRONT CATEGORY:", fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = mutedColor)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Birthday", "Festival", "Anniversary", "Love", "Friendship", "Celebration").forEach { cat ->
                                        ClayButton(
                                            onClick = { customCategory = cat },
                                            variant = if (customCategory.equals(cat, true)) ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                                            isDark = isDark,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(cat, fontSize = 10.sp, maxLines = 1)
                                        }
                                    }
                                }

                                ClayTextField(
                                    value = customDescription,
                                    onValueChange = { customDescription = it },
                                    label = "Storefront Description",
                                    isDark = isDark
                                )
                            }
                        }
                    }
                }

                // TAB 2: DECLARED VARIANTS & THEMES MANAGER
                2 -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Amber)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "👥 Package Variants & Themes (${configuredVariantsList.size})",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor
                                    )
                                    ClayPill(
                                        text = "${enabledVariants.size} Active",
                                        color = ClayColors.Emerald,
                                        isDark = isDark
                                    )
                                }

                                if (configuredVariantsList.isEmpty() && allCandidateVariants.isEmpty()) {
                                    Text(
                                        text = "Single-Edition Template (No multi-variant matrix declared in wishmagic.json).",
                                        fontSize = 11.sp,
                                        color = mutedColor
                                    )
                                } else {
                                    configuredVariantsList.forEach { vNode ->
                                        val isEn = enabledVariants.contains(vNode.relationship) || vNode.enabled
                                        val pCol = runCatching { Color(android.graphics.Color.parseColor(vNode.themePrimary)) }.getOrDefault(ClayColors.Amber)
                                        val sCol = runCatching { Color(android.graphics.Color.parseColor(vNode.themeSecondary)) }.getOrDefault(ClayColors.Pink)
                                        val bgCol = runCatching { Color(android.graphics.Color.parseColor(vNode.themeBackground)) }.getOrDefault(Color(0xFF160702))

                                        ClayWellBox(isDark = isDark, contentPadding = PaddingValues(10.dp)) {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        // Palette Swatches
                                                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                            listOf(pCol, sCol, bgCol).forEach { sw ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(13.dp)
                                                                        .clip(CircleShape)
                                                                        .background(sw)
                                                                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                                                )
                                                            }
                                                        }
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = vNode.relationshipLabel.ifBlank { vNode.name },
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Black,
                                                                    color = textColor,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                ClayPill(
                                                                    text = vNode.kind.uppercase(),
                                                                    color = if (vNode.kind == "theme") ClayColors.Amber else ClayColors.Primary,
                                                                    isDark = isDark
                                                                )
                                                            }
                                                            Text(
                                                                text = "Slug: ${vNode.slug} • ${vNode.messages.size} default texts",
                                                                fontSize = 9.5.sp,
                                                                color = mutedColor
                                                            )
                                                        }
                                                    }

                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        ClayButton(
                                                            onClick = { studioTab = 0 },
                                                            variant = ClayButtonVariant.SECONDARY,
                                                            isDark = isDark,
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(12.dp))
                                                            Spacer(Modifier.width(3.dp))
                                                            Text("Preview", fontSize = 9.5.sp)
                                                        }
                                                        ClaySwitch(
                                                            checked = isEn,
                                                            onCheckedChange = { checked ->
                                                                enabledVariants = if (checked) {
                                                                    (enabledVariants + vNode.relationship).distinct()
                                                                } else if (enabledVariants.size > 1) {
                                                                    enabledVariants - vNode.relationship
                                                                } else enabledVariants
                                                                configuredVariantsList = configuredVariantsList.map {
                                                                    if (it.slug == vNode.slug) it.copy(enabled = checked) else it
                                                                }
                                                            },
                                                            isDark = isDark
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 3: 3-LEVEL SECTIONS & CUSTOMIZER FIELDS INSPECTOR
                3 -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🧩 3-Level Sections (${configuredSections.size}) — Syncs with Live Preview",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )

                                configuredSections.forEach { sec ->
                                    ClayWellBox(isDark = isDark, contentPadding = PaddingValues(9.dp)) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = sec.label,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = textColor
                                                        )
                                                        ClayPill(
                                                            text = if (sec.required) "REQUIRED" else "OPTIONAL",
                                                            color = if (sec.required) ClayColors.Amber else ClayColors.Primary,
                                                            isDark = isDark
                                                        )
                                                    }
                                                    Text(
                                                        text = "ID: ${sec.id} • ${sec.fields.size} fields • ${sec.subsections.size} subsections",
                                                        fontSize = 9.5.sp,
                                                        color = mutedColor
                                                    )
                                                }
                                                ClaySwitch(
                                                    checked = if (sec.required) true else sec.enabled,
                                                    onCheckedChange = { checked ->
                                                        if (!sec.required) {
                                                            configuredSections = configuredSections.map {
                                                                if (it.id == sec.id) it.copy(enabled = checked) else it
                                                            }
                                                        }
                                                    },
                                                    isDark = isDark
                                                )
                                            }

                                            if (sec.subsections.isNotEmpty()) {
                                                Column(
                                                    modifier = Modifier.padding(start = 10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    sec.subsections.forEach { sub ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = "↳ ${sub.label} (${sub.id})",
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = textColor,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            ClaySwitch(
                                                                checked = if (sub.required) true else sub.enabled,
                                                                onCheckedChange = { subChecked ->
                                                                    if (!sub.required) {
                                                                        configuredSections = configuredSections.map { parent ->
                                                                            if (parent.id == sec.id) {
                                                                                parent.copy(
                                                                                    subsections = parent.subsections.map { s ->
                                                                                        if (s.id == sub.id) s.copy(enabled = subChecked) else s
                                                                                    }
                                                                                )
                                                                            } else parent
                                                                        }
                                                                    }
                                                                },
                                                                isDark = isDark
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (pkg.fields.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "📝 Declared Customizer Fields (${pkg.fields.size})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor
                                    )
                                    pkg.fields.forEach { fld ->
                                        ClayWellBox(isDark = isDark, contentPadding = PaddingValues(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = fld.label,
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = textColor
                                                        )
                                                        ClayPill(
                                                            text = fld.type.uppercase(),
                                                            color = ClayColors.Pink,
                                                            isDark = isDark
                                                        )
                                                        if (fld.required) {
                                                            ClayPill(text = "REQ", color = ClayColors.Amber, isDark = isDark)
                                                        }
                                                    }
                                                    Text(
                                                        text = "ID: ${fld.id} • Section: ${fld.section}" + if (fld.defaultValue.isNotBlank()) " • Default: ${fld.defaultValue.take(28)}" else "",
                                                        fontSize = 9.5.sp,
                                                        color = mutedColor,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 4: CLOUD ROUTING, UTP AUDIT CHECKLIST, LINKS & FESTIVAL METADATA
                else -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Sky, ClayColors.Primary, ClayColors.Emerald)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                Text(
                                    text = "✅ UTP v${pkg.detectedSchemaVersion} Pre-Flight Audit Checklist",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )

                                pkg.validationChecks.forEach { (checkLabel, passed) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = checkLabel,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusBadgePill(
                                            text = if (passed) "PASS" else "WARN",
                                            isSuccess = passed,
                                            isDark = isDark
                                        )
                                    }
                                }

                                HorizontalDivider(color = mutedColor.copy(alpha = 0.2f))

                                Text(
                                    text = "☁️ Cloud Storage File Routing (${pkg.fileEntries.size} files)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )

                                pkg.fileEntries.forEach { fe ->
                                    val pillCol = when (fe.category) {
                                        "image" -> ClayColors.Amber
                                        "manifest" -> ClayColors.Emerald
                                        "ignored" -> ClayColors.Rose
                                        else -> ClayColors.Sky
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${fe.path} (${fe.sizeKb} KB)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        ClayPill(
                                            text = fe.cloudTarget,
                                            color = pillCol,
                                            isDark = isDark
                                        )
                                    }
                                }

                                HorizontalDivider(color = mutedColor.copy(alpha = 0.2f))

                                Text(
                                    text = "🪔 Advisory Festival Metadata",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                                ClayTextField(
                                    value = festivalBadge,
                                    onValueChange = { festivalBadge = it },
                                    label = "Storefront Festival / Edition Badge",
                                    placeholder = "✨ Special Edition",
                                    isDark = isDark
                                )
                                ClayTextField(
                                    value = festivalTagsCsv,
                                    onValueChange = { festivalTagsCsv = it },
                                    label = "Festival Tags (Comma Separated)",
                                    placeholder = "birthday, celebration, diwali",
                                    isDark = isDark
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Empty State: Select a ZIP to inspect & preview live
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary, ClayColors.Pink)),
                    isDark = isDark
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "📦 Select a Template .ZIP to See Live Preview & Full Details",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "Jaise hi aap koi .ZIP select ya share karenge, app ke andar hi uska Live Interactive Preview, Cover Image Changer, Variants & Themes Switcher, 3-Level Sections, Customizer Fields, aur Cloud File Routing (ImgBB + Backblaze B2 + D1) dikhega!",
                            fontSize = 11.sp,
                            color = mutedColor
                        )
                        GradientPillButton(
                            text = "Browse & Select Template .ZIP",
                            icon = Icons.Default.UploadFile,
                            onClick = { zipPicker.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink, ClayColors.Primary)),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🤖 UTP v1.4.0 AI Studio Master Prompt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                    ClayButton(
                        onClick = {
                            copyTextToClipboard(context, "UTP v1.4.0 Master Prompt", UTP_V140_MASTER_PROMPT)
                            viewModel.showToast("📋 Master Prompt Copied!", "Paste into Gemini, AI Studio, or ChatGPT!")
                        },
                        variant = ClayButtonVariant.PINK,
                        isDark = isDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy UTP v1.4.0 Prompt", fontSize = 10.5.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun UsersScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val users by viewModel.users.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateUserModal by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    val filters = listOf("All", "VIP Pass", "Free", "Active", "Suspended")
    val filtered = users.filter { u ->
        val matchFilter = when (selectedFilter) {
            "VIP Pass" -> u.vipActive
            "Free" -> !u.vipActive
            "Active" -> u.status.equals("active", true)
            "Suspended" -> !u.status.equals("active", true)
            else -> true
        }
        val matchSearch = searchQuery.isBlank() ||
            u.displayName.contains(searchQuery, true) ||
            u.email.contains(searchQuery, true)
        matchFilter && matchSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Users & VIP Directory",
                titlePrefix = "Users &",
                pinkAccentWord = "VIP Creators",
                titleSuffix = "(${filtered.size})",
                primaryButtonText = "+ Add User",
                primaryButtonIcon = Icons.Default.PersonAdd,
                onPrimaryClick = { showCreateUserModal = true },
                secondaryButtonText = "↻ Sync",
                secondaryButtonIcon = Icons.Default.Refresh,
                onSecondaryClick = { viewModel.syncLive() }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { f ->
                    ClayButton(
                        onClick = { selectedFilter = f },
                        variant = if (selectedFilter == f) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Text(f, fontSize = 10.5.sp, maxLines = 1)
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { u ->
            val isSuspended = u.status.equals("suspended", true) || u.status.equals("banned", true)
            TopAccentCard(
                accentBrush = if (u.vipActive) {
                    Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink))
                } else {
                    Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Sky))
                },
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(u.displayName, fontSize = 14.5.sp, fontWeight = FontWeight.Black, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (u.vipActive) {
                                    ClayPill(text = "👑 VIP", color = ClayColors.Amber, isDark = isDark)
                                }
                                StatusBadgePill(text = u.status.uppercase(), isSuccess = !isSuspended, isDark = isDark)
                            }
                            Text(u.email, fontSize = 11.sp, color = mutedColor)
                            Text("Role: ${u.role.uppercase()} • Wishes: ${u.wishesCount} • Referrals: ${u.referralsCount}/7", fontSize = 10.sp, color = mutedColor)
                        }
                        CompactIconButton(onClick = { viewModel.deleteUserAccount(u.id, u.email) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ClayColors.Rose, modifier = Modifier.size(18.dp))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClayButton(
                            onClick = { viewModel.toggleUserVip(u.id, u.email, !u.vipActive) },
                            variant = if (u.vipActive) ClayButtonVariant.SECONDARY else ClayButtonVariant.AMBER,
                            isDark = isDark
                        ) {
                            Text(if (u.vipActive) "Revoke VIP" else "👑 Grant VIP", fontSize = 10.sp, maxLines = 1)
                        }
                        ClayButton(
                            onClick = {
                                val next = if (isSuspended) "active" else "suspended"
                                viewModel.updateUserStatus(u.id, u.email, next)
                            },
                            variant = if (isSuspended) ClayButtonVariant.EMERALD else ClayButtonVariant.DANGER,
                            isDark = isDark
                        ) {
                            Text(if (isSuspended) "Activate" else "Suspend", fontSize = 10.sp, maxLines = 1)
                        }
                        ClayButton(
                            onClick = { viewModel.revokeUserSessions(u.id, u.email) },
                            variant = ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Text("🔒 Revoke", fontSize = 10.sp, maxLines = 1)
                        }
                        ClayButton(
                            onClick = { viewModel.grantReferralCredit(u.email, 1) },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Text("+1 Free Link", fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    if (showCreateUserModal) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var vip by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showCreateUserModal = false }) {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Create User Account in D1", fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    ClayTextField(value = name, onValueChange = { name = it }, label = "Full Name", placeholder = "Priya Sharma", isDark = isDark)
                    ClayTextField(value = email, onValueChange = { email = it }, label = "Email Address", placeholder = "priya@gmail.com", isDark = isDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grant VIP Celebration Pass", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = textColor)
                        ClaySwitch(checked = vip, onCheckedChange = { vip = it }, isDark = isDark)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateUserModal = false }) { Text("Cancel", color = mutedColor) }
                        Spacer(Modifier.width(8.dp))
                        ClayButton(
                            onClick = {
                                if (email.isNotBlank()) {
                                    viewModel.createUserAccount(email, name, if (vip) "vip" else "creator", vip)
                                    showCreateUserModal = false
                                }
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Text("Create User", fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    var showCompPassModal by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("All") }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    val tabs = listOf("All", "Paid", "Comp Passes")
    val filtered = orders.filter { o ->
        val matchTab = when (selectedTab) {
            "Paid" -> !o.isComplimentary && o.status.equals("PAID", true)
            "Comp Passes" -> o.isComplimentary
            else -> true
        }
        val matchSearch = searchQuery.isBlank() ||
            o.id.contains(searchQuery, true) ||
            o.customerEmail.contains(searchQuery, true) ||
            o.wishSlug.contains(searchQuery, true)
        matchTab && matchSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Razorpay & Comp VIP Passes",
                titlePrefix = "Orders &",
                pinkAccentWord = "Payments",
                titleSuffix = "(${filtered.size})",
                primaryButtonText = "+ Comp VIP Pass",
                primaryButtonIcon = Icons.Default.CardGiftcard,
                onPrimaryClick = { showCompPassModal = true },
                secondaryButtonText = "↻ Sync",
                secondaryButtonIcon = Icons.Default.Refresh,
                onSecondaryClick = { viewModel.syncLive() }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tabs.forEach { t ->
                    ClayButton(
                        onClick = { selectedTab = t },
                        variant = if (selectedTab == t) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Text(t, fontSize = 10.5.sp, maxLines = 1)
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { ord ->
            val isPaid = ord.status.equals("PAID", true)
            TopAccentCard(
                accentBrush = if (ord.isComplimentary) {
                    Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink))
                } else {
                    Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary))
                },
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(ord.id, fontSize = 13.5.sp, fontWeight = FontWeight.Black, color = textColor)
                            StatusBadgePill(text = ord.status, isSuccess = isPaid, isDark = isDark)
                            if (ord.isComplimentary) {
                                ClayPill(text = "🎁 COMP", color = ClayColors.Amber, isDark = isDark)
                            }
                        }
                        Text(
                            text = "₹${ord.amount.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = ClayColors.Emerald
                        )
                    }
                    Text("${ord.planName} • ${ord.customerEmail}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text("Gateway: ${ord.paymentGateway} • Wish: /w/${ord.wishSlug.ifBlank { "auto" }} • ${ord.createdAt}", fontSize = 10.sp, color = mutedColor)
                    if (ord.wishSlug.isNotBlank()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClayButton(
                                onClick = {
                                    val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$base/w/${ord.wishSlug}")))
                                    }
                                },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Open /w/${ord.wishSlug}", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCompPassModal) {
        var recipientName by remember { mutableStateOf("") }
        var contact by remember { mutableStateOf("") }
        var templateName by remember { mutableStateOf("Royal Festival of Lights") }
        var reason by remember { mutableStateOf("Complimentary VIP Gift Pass from Admin") }

        Dialog(onDismissRequest = { showCompPassModal = false }) {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Issue Complimentary VIP Gift Pass", fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    ClayTextField(value = recipientName, onValueChange = { recipientName = it }, label = "Recipient Name", placeholder = "Ananya Verma", isDark = isDark)
                    ClayTextField(value = contact, onValueChange = { contact = it }, label = "Customer Email / WhatsApp", placeholder = "ananya@gmail.com", isDark = isDark)
                    ClayTextField(value = templateName, onValueChange = { templateName = it }, label = "Template Name", isDark = isDark)
                    ClayTextField(value = reason, onValueChange = { reason = it }, label = "Admin Note", isDark = isDark)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCompPassModal = false }) { Text("Cancel", color = mutedColor) }
                        Spacer(Modifier.width(8.dp))
                        ClayButton(
                            onClick = {
                                if (recipientName.isNotBlank()) {
                                    viewModel.issueComplimentaryVipPass(recipientName, contact, templateName, reason)
                                    showCompPassModal = false
                                }
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Text("Issue VIP Pass", fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OccasionsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val occasions by viewModel.occasions.collectAsState()
    val siteSettings by viewModel.siteSettings.collectAsState()

    var newName by remember { mutableStateOf("") }
    var newIcon by remember { mutableStateOf("🪔") }
    var newTag by remember { mutableStateOf("") }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Storefront Occasion Pills",
                titlePrefix = "Occasions &",
                pinkAccentWord = "Categories",
                titleSuffix = "(${occasions.size})"
            )
        }

        item {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add Storefront Occasion Pill", fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.width(76.dp)) {
                            ClayTextField(value = newIcon, onValueChange = { newIcon = it }, label = "Emoji", isDark = isDark)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(value = newName, onValueChange = { newName = it }, label = "Occasion Name", placeholder = "Bhai Dooj", isDark = isDark)
                        }
                    }
                    ClayButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                viewModel.addOccasionCategory(newName, newIcon)
                                newName = ""
                            }
                        },
                        variant = ClayButtonVariant.PINK,
                        isDark = isDark
                    ) {
                        Text("+ Add to Live Storefront", fontSize = 10.5.sp, maxLines = 1)
                    }
                }
            }
        }

        item {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Primary)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Popular Storefront Search Tags", fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        siteSettings.popularTags.forEach { tag ->
                            ClayPill(
                                text = "#$tag  ✕",
                                color = ClayColors.Primary,
                                isDark = isDark,
                                modifier = Modifier.clickable {
                                    val next = siteSettings.popularTags.filterNot { it == tag }
                                    viewModel.updateSiteSettings(popularTags = next, label = "Removed tag #$tag")
                                }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(value = newTag, onValueChange = { newTag = it }, label = "New Search Tag", placeholder = "Karwa Chauth", isDark = isDark)
                        }
                        ClayButton(
                            onClick = {
                                if (newTag.isNotBlank()) {
                                    val next = (siteSettings.popularTags + newTag.trim()).distinct()
                                    viewModel.updateSiteSettings(popularTags = next, label = "Added search tag #${newTag.trim()}!")
                                    newTag = ""
                                }
                            },
                            variant = ClayButtonVariant.AMBER,
                            isDark = isDark
                        ) {
                            Text("+ Add Tag", fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        items(occasions, key = { it.id }) { occ ->
            ClayCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${occ.icon}  ${occ.name}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                        Text("Slug: /${occ.slug} • Templates: ${occ.count}", fontSize = 10.5.sp, color = mutedColor)
                    }
                    CompactIconButton(onClick = { viewModel.deleteOccasionCategory(occ.id, occ.name) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ClayColors.Rose, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * COMPLETE SEPARATED ADMIN & USER MEDIA LIBRARY (`MediaLibraryScreen`):
 * 1. Keeps Admin Uploaded Media (`🛡️ Admin Uploaded Media`) and User Uploaded Media (`👤 User Uploaded Media`)
 *    in two completely separate, clearly marked sections!
 * 2. Allows filtering by Uploader Source (`All Sections`, `🛡️ Admin Only`, `👤 User Only`),
 *    by Media Type (`All`, `🖼️ Photos`, `🎵 Audio`, `🎬 Video`, `🎨 Template Slots`),
 *    AND by Specific User (`All Users` or pick any specific user by Name/Email/ID to inspect all photos, audios & videos uploaded by that user)!
 */
@Composable
fun MediaLibraryScreen(
    viewModel: AdminViewModel,
    onlyAudio: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val mediaAssets by viewModel.mediaAssets.collectAsState()
    val mediaSummary by viewModel.mediaSummary.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    // Section Filter: "both" (shows both separate sections), "admin" (only Admin section), "user" (only User section)
    var selectedUploaderSection by remember { mutableStateOf("both") }
    var selectedTypeFilter by remember(onlyAudio) { mutableStateOf(if (onlyAudio) "audio" else "all") }
    var selectedUserFilter by remember { mutableStateOf("all") }
    var localUserSearchQuery by remember { mutableStateOf("") }

    var uploadTitle by remember { mutableStateOf("") }
    var uploadArtist by remember { mutableStateOf("") }
    var uploadType by remember(onlyAudio) { mutableStateOf(if (onlyAudio) "audio" else "auto") }
    var setAsHeroOnUpload by remember { mutableStateOf(false) }
    var showUploadPanel by remember { mutableStateOf(false) }

    var externalUrl by remember { mutableStateOf("") }
    var lightboxAsset by remember { mutableStateOf<MediaAssetItem?>(null) }
    var replacingTemplateAsset by remember { mutableStateOf<MediaAssetItem?>(null) }

    // Built-in Live Audio Player State
    var playingAsset by remember { mutableStateOf<MediaAssetItem?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var audioStatusText by remember { mutableStateOf("") }
    val mediaPlayerRef = remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                mediaPlayerRef.value?.stop()
                mediaPlayerRef.value?.release()
            }
            mediaPlayerRef.value = null
        }
    }

    fun playOrToggleAudio(asset: MediaAssetItem) {
        val streamTarget = asset.streamUrl.ifBlank { asset.url }
        if (streamTarget.isBlank()) {
            viewModel.showToast("⚠️ Audio URL Missing", "No stream URL found for ${asset.title}")
            return
        }
        if (playingAsset?.id == asset.id && mediaPlayerRef.value != null) {
            val mp = mediaPlayerRef.value!!
            runCatching {
                if (mp.isPlaying) {
                    mp.pause()
                    isAudioPlaying = false
                    audioStatusText = "Paused"
                } else {
                    mp.start()
                    isAudioPlaying = true
                    audioStatusText = "Playing from ${asset.provider}"
                }
            }
            return
        }

        runCatching {
            mediaPlayerRef.value?.stop()
            mediaPlayerRef.value?.release()
        }
        playingAsset = asset
        isAudioPlaying = false
        audioStatusText = "Connecting to ${asset.provider} stream..."

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(streamTarget)
                setOnPreparedListener { player ->
                    player.start()
                    isAudioPlaying = true
                    audioStatusText = "🔊 Streaming Live (${asset.format} • ${asset.sizeFormatted})"
                }
                setOnCompletionListener {
                    isAudioPlaying = false
                    audioStatusText = "Playback Finished"
                }
                setOnErrorListener { _, _, _ ->
                    if (asset.url.isNotBlank() && asset.url != streamTarget) {
                        runCatching {
                            reset()
                            setDataSource(asset.url)
                            prepareAsync()
                            return@setOnErrorListener true
                        }
                    }
                    isAudioPlaying = false
                    audioStatusText = "Stream error — tap Open"
                    true
                }
                prepareAsync()
            }
            mediaPlayerRef.value = mp
        } catch (e: Exception) {
            audioStatusText = "Error: ${e.message}"
        }
    }

    fun stopAudioPlayer() {
        runCatching {
            mediaPlayerRef.value?.stop()
            mediaPlayerRef.value?.release()
        }
        mediaPlayerRef.value = null
        playingAsset = null
        isAudioPlaying = false
    }

    val mediaFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadMediaFileFromUri(
                context = context,
                uri = uri,
                customTitle = uploadTitle,
                forcedType = uploadType,
                artist = uploadArtist,
                setAsHeroImage = setAsHeroOnUpload
            )
            uploadTitle = ""
            uploadArtist = ""
        }
    }

    val heroPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadMediaFileFromUri(
                context = context,
                uri = uri,
                customTitle = "Storefront Hero Banner",
                forcedType = "image",
                category = "Hero & Showcase",
                setAsHeroImage = true
            )
        }
    }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    // Partition all assets into Admin Uploads vs User Uploads
    val allAdminAssets = remember(mediaAssets) {
        mediaAssets.filter { !it.uploaderRole.equals("user", ignoreCase = true) }
    }
    val allUserAssets = remember(mediaAssets) {
        mediaAssets.filter { it.uploaderRole.equals("user", ignoreCase = true) }
    }

    // Distinct Users who uploaded media (keyed by email or userName)
    val distinctUsers = remember(allUserAssets) {
        allUserAssets
            .groupBy { item ->
                item.userEmail.ifBlank { item.userId.ifBlank { item.userName.ifBlank { "Customer" } } }.lowercase()
            }
            .map { (uKey, list) ->
                val first = list.first()
                val displayLabel = first.userName.ifBlank { first.userEmail.ifBlank { uKey } }
                Triple(uKey, displayLabel, list.size)
            }
            .sortedByDescending { it.third }
    }

    fun matchesTypeAndSearch(item: MediaAssetItem): Boolean {
        val matchType = when (selectedTypeFilter) {
            "audio" -> item.type == "audio"
            "image" -> item.type == "image"
            "template_image" -> item.isTemplateImage
            "video" -> item.type == "video"
            else -> true
        }
        val q = localUserSearchQuery.trim()
        val matchSearch = q.isBlank() ||
            item.title.contains(q, true) ||
            item.userName.contains(q, true) ||
            item.userEmail.contains(q, true) ||
            item.wishSlug.contains(q, true) ||
            item.recipientName.contains(q, true) ||
            item.category.contains(q, true) ||
            item.templateName.contains(q, true)
        return matchType && matchSearch
    }

    val filteredAdminAssets = remember(allAdminAssets, selectedTypeFilter, localUserSearchQuery) {
        allAdminAssets.filter { matchesTypeAndSearch(it) }
    }

    val filteredUserAssets = remember(allUserAssets, selectedTypeFilter, selectedUserFilter, localUserSearchQuery) {
        allUserAssets.filter { item ->
            val uKey = item.userEmail.ifBlank { item.userId.ifBlank { item.userName.ifBlank { "Customer" } } }.lowercase()
            val matchUser = selectedUserFilter == "all" || uKey.equals(selectedUserFilter, ignoreCase = true)
            matchUser && matchesTypeAndSearch(item)
        }
    }

    @Composable
    fun RenderMediaAssetCard(item: MediaAssetItem, isUserUpload: Boolean) {
        val isAudio = item.type == "audio"
        val isVideo = item.type == "video"
        val isThisPlaying = playingAsset?.id == item.id && isAudioPlaying

        TopAccentCard(
            accentBrush = when {
                isUserUpload -> Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Amber))
                isAudio -> Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink))
                isVideo -> Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Sky))
                else -> Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary))
            },
            isDark = isDark,
            contentPadding = PaddingValues(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (item.type == "image") {
                            AsyncImage(
                                model = item.thumbnailUrl.ifBlank { item.url },
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(ClayColors.Primary.copy(alpha = 0.15f))
                                    .clickable { lightboxAsset = item }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        if (isAudio) Brush.linearGradient(listOf(ClayColors.Amber, ClayColors.Pink))
                                        else Brush.linearGradient(listOf(ClayColors.Primary, ClayColors.Sky))
                                    )
                                    .clickable { if (isAudio) playOrToggleAudio(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAudio) {
                                        if (isThisPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled
                                    } else Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ClayPill(
                                    text = if (isUserUpload) "👤 USER UPLOAD" else "🛡️ ADMIN UPLOAD",
                                    color = if (isUserUpload) ClayColors.Pink else ClayColors.Emerald,
                                    isDark = isDark
                                )
                                ClayPill(
                                    text = item.format.ifBlank { item.type.uppercase() },
                                    color = if (isAudio) ClayColors.Amber else ClayColors.Primary,
                                    isDark = isDark
                                )
                                if (item.isTemplateImage) {
                                    ClayPill(text = "SLOT", color = ClayColors.Sky, isDark = isDark)
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (isUserUpload) {
                                val uploaderInfo = buildString {
                                    append("👤 By: ${item.userName.ifBlank { "Customer" }}")
                                    if (item.userEmail.isNotBlank()) append(" (${item.userEmail})")
                                }
                                Text(
                                    text = uploaderInfo,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayColors.Pink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.wishSlug.isNotBlank()) {
                                    Text(
                                        text = "🎁 Wish: /w/${item.wishSlug}${if (item.recipientName.isNotBlank()) " • For ${item.recipientName}" else ""} • ${item.createdAt}",
                                        fontSize = 10.sp,
                                        color = mutedColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text(
                                    text = "${item.provider} • ${item.sizeFormatted} • ${item.category} • ${item.createdAt}",
                                    fontSize = 10.sp,
                                    color = mutedColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (!item.isDemo) {
                        CompactIconButton(onClick = { viewModel.deleteMediaAsset(item) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ClayColors.Rose, modifier = Modifier.size(17.dp))
                        }
                    }
                }

                // Compact Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isAudio) {
                        ClayButton(
                            onClick = { playOrToggleAudio(item) },
                            variant = if (isThisPlaying) ClayButtonVariant.AMBER else ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Icon(
                                imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (isThisPlaying) "Pause" else "▶ Play Audio", fontSize = 10.sp, maxLines = 1)
                        }
                    } else if (item.type == "image") {
                        ClayButton(
                            onClick = { lightboxAsset = item },
                            variant = ClayButtonVariant.PRIMARY,
                            isDark = isDark
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Preview", fontSize = 10.sp, maxLines = 1)
                        }
                        if (!isUserUpload) {
                            ClayButton(
                                onClick = { viewModel.setStorefrontHeroImage(item.rawUrl.ifBlank { item.url }, item.title) },
                                variant = ClayButtonVariant.AMBER,
                                isDark = isDark
                            ) {
                                Text("🌟 Set Hero", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                        if (item.isTemplateImage) {
                            ClayButton(
                                onClick = { replacingTemplateAsset = item },
                                variant = ClayButtonVariant.EMERALD,
                                isDark = isDark
                            ) {
                                Text("🔄 Replace Slot", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }

                    if (isUserUpload) {
                        val uKey = item.userEmail.ifBlank { item.userId.ifBlank { item.userName } }.lowercase()
                        if (uKey.isNotBlank() && selectedUserFilter != uKey) {
                            ClayButton(
                                onClick = {
                                    selectedUploaderSection = "user"
                                    selectedUserFilter = uKey
                                },
                                variant = ClayButtonVariant.AMBER,
                                isDark = isDark
                            ) {
                                Text("👤 Filter This User", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                        if (item.wishSlug.isNotBlank()) {
                            ClayButton(
                                onClick = {
                                    val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$base/w/${item.wishSlug}")))
                                    }
                                },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark
                            ) {
                                Text("🎁 Open /w/${item.wishSlug}", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }

                    ClayButton(
                        onClick = {
                            val targetUrl = item.url.ifBlank { item.streamUrl }
                            copyTextToClipboard(context, item.title, targetUrl)
                            viewModel.showToast("📋 Link Copied!", targetUrl)
                        },
                        variant = ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Copy URL", fontSize = 10.sp, maxLines = 1)
                    }

                    ClayButton(
                        onClick = {
                            val openUrl = item.url.ifBlank { item.streamUrl }
                            if (openUrl.isNotBlank()) {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(openUrl)))
                                }
                            }
                        },
                        variant = ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Open", fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Admin & User Media Vault",
                titlePrefix = if (onlyAudio) "Music &" else "Media",
                pinkAccentWord = if (onlyAudio) "Audio Vault" else "Library",
                titleSuffix = "(${mediaAssets.size})",
                primaryButtonText = "+ Upload Media",
                primaryButtonIcon = Icons.Default.CloudUpload,
                onPrimaryClick = { showUploadPanel = !showUploadPanel },
                secondaryButtonText = "🔧 Sync B2 & D1",
                secondaryButtonIcon = Icons.Default.Sync,
                onSecondaryClick = { viewModel.healAndReconcileMedia() }
            )
        }

        // Sticky Live Audio Player Bar when an audio track is playing
        playingAsset?.let { track ->
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink, ClayColors.Primary)),
                    isDark = isDark,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(ClayColors.Pink, ClayColors.Primary))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = audioStatusText.ifBlank { "${track.provider} • ${track.format}" },
                                    fontSize = 10.sp,
                                    color = ClayColors.Emerald,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ClayButton(
                                onClick = { playOrToggleAudio(track) },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark
                            ) {
                                Text(if (isAudioPlaying) "⏸" else "▶", fontSize = 11.sp)
                            }
                            ClayButton(
                                onClick = { stopAudioPlayer() },
                                variant = ClayButtonVariant.DANGER,
                                isDark = isDark
                            ) {
                                Text("⏹", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Collapsible Upload & Hero Manager Panel
        if (showUploadPanel) {
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary)),
                    isDark = isDark,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "☁️ Upload Admin Media (B2 / ImgBB) & Hero Photo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor
                            )
                            CompactIconButton(onClick = { showUploadPanel = false }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedColor, modifier = Modifier.size(16.dp))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                ClayTextField(
                                    value = uploadTitle,
                                    onValueChange = { uploadTitle = it },
                                    label = "Asset Title",
                                    placeholder = "Festive Flute Chimes",
                                    isDark = isDark
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ClayTextField(
                                    value = uploadArtist,
                                    onValueChange = { uploadArtist = it },
                                    label = "Artist / Tag",
                                    placeholder = "WishMagic Studio",
                                    isDark = isDark
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "auto" to "✨ Auto-Detect",
                                "image" to "🖼️ Photo (ImgBB)",
                                "audio" to "🎵 Audio (B2)",
                                "video" to "🎬 Video (B2)"
                            ).forEach { (tKey, tLabel) ->
                                ClayButton(
                                    onClick = { uploadType = tKey },
                                    variant = if (uploadType == tKey) ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                                    isDark = isDark
                                ) {
                                    Text(tLabel, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClayButton(
                                onClick = {
                                    val mimes = when (uploadType) {
                                        "audio" -> arrayOf("audio/*", "*/*")
                                        "image" -> arrayOf("image/*")
                                        "video" -> arrayOf("video/*")
                                        else -> arrayOf("image/*", "audio/*", "video/*", "*/*")
                                    }
                                    mediaFilePicker.launch(mimes)
                                },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Upload File", fontSize = 10.5.sp, maxLines = 1)
                            }
                            ClayButton(
                                onClick = { heroPhotoPicker.launch(arrayOf("image/*")) },
                                variant = ClayButtonVariant.AMBER,
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🌟 Upload Hero Photo", fontSize = 10.5.sp, maxLines = 1)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) {
                                ClayTextField(
                                    value = externalUrl,
                                    onValueChange = { externalUrl = it },
                                    label = "Or Register Direct CDN URL",
                                    placeholder = "https://...",
                                    isDark = isDark
                                )
                            }
                            ClayButton(
                                onClick = {
                                    if (externalUrl.isNotBlank()) {
                                        val t = if (onlyAudio || externalUrl.endsWith(".mp3", true)) "audio" else "image"
                                        viewModel.addMediaAsset(uploadTitle.ifBlank { "Cloud Asset" }, externalUrl, t)
                                        externalUrl = ""
                                        uploadTitle = ""
                                    }
                                },
                                variant = ClayButtonVariant.EMERALD,
                                isDark = isDark
                            ) {
                                Text("+ Add URL", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // MASTER FILTER CONTROL CARD:
        // 1) Section Selector (Both Sections / Admin Uploads Only / User Uploads Only)
        // 2) Media Type Filter (All / Photos / Audio / Video / Template Slots)
        // 3) User Filter (All Users / Specific User + Search by User Name/Email/Wish)
        item {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
                isDark = isDark,
                contentPadding = PaddingValues(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. Uploader Source Filter (Admin vs User)
                    Text(
                        text = "1. FILTER BY UPLOAD SOURCE (ADMIN VS USER):",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = mutedColor
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClayButton(
                            onClick = { selectedUploaderSection = "both" },
                            variant = if (selectedUploaderSection == "both") ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Text("🌐 Both Sections (${mediaAssets.size})", fontSize = 10.sp, maxLines = 1)
                        }
                        ClayButton(
                            onClick = { selectedUploaderSection = "admin" },
                            variant = if (selectedUploaderSection == "admin") ClayButtonVariant.EMERALD else ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Text("🛡️ Admin Uploads (${allAdminAssets.size})", fontSize = 10.sp, maxLines = 1)
                        }
                        ClayButton(
                            onClick = { selectedUploaderSection = "user" },
                            variant = if (selectedUploaderSection == "user") ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Text("👤 User Uploads (${allUserAssets.size})", fontSize = 10.sp, maxLines = 1)
                        }
                    }

                    // 2. Media Type Filter (Photo / Audio / Video)
                    Text(
                        text = "2. FILTER BY MEDIA TYPE (PHOTO / AUDIO / VIDEO):",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = mutedColor
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val photoCnt = mediaAssets.count { it.type == "image" }
                        val audioCnt = mediaAssets.count { it.type == "audio" }
                        val videoCnt = mediaAssets.count { it.type == "video" }
                        val tplCnt = mediaAssets.count { it.isTemplateImage }
                        listOf(
                            "all" to "All Types (${mediaAssets.size})",
                            "image" to "🖼️ Photos ($photoCnt)",
                            "audio" to "🎵 Audio ($audioCnt)",
                            "video" to "🎬 Video ($videoCnt)",
                            "template_image" to "🎨 Template Slots ($tplCnt)"
                        ).forEach { (key, label) ->
                            ClayButton(
                                onClick = { selectedTypeFilter = key },
                                variant = if (selectedTypeFilter == key) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                isDark = isDark
                            ) {
                                Text(label, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }

                    // 3. Filter by Specific User (Who Uploaded the Photo/Audio/Video)
                    Text(
                        text = "3. FILTER BY USER (WHO UPLOADED):",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = mutedColor
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClayButton(
                            onClick = { selectedUserFilter = "all" },
                            variant = if (selectedUserFilter == "all") ClayButtonVariant.AMBER else ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Text("👥 All Users (${distinctUsers.size})", fontSize = 10.sp, maxLines = 1)
                        }
                        distinctUsers.forEach { (uKey, uLabel, uCount) ->
                            ClayButton(
                                onClick = {
                                    selectedUserFilter = uKey
                                    if (selectedUploaderSection == "admin") {
                                        selectedUploaderSection = "user"
                                    }
                                },
                                variant = if (selectedUserFilter.equals(uKey, ignoreCase = true)) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                isDark = isDark
                            ) {
                                Text("👤 $uLabel ($uCount)", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }

                    ClayTextField(
                        value = localUserSearchQuery,
                        onValueChange = { localUserSearchQuery = it },
                        placeholder = "Search by user name, email, wish slug, or file title...",
                        isDark = isDark
                    )
                }
            }
        }

        // =====================================================================
        // SECTION 1: 🛡️ ADMIN UPLOADED MEDIA (COMPLETELY SEPARATE SECTION)
        // =====================================================================
        if (selectedUploaderSection == "both" || selectedUploaderSection == "admin") {
            item {
                val admPhotos = filteredAdminAssets.count { it.type == "image" }
                val admAudios = filteredAdminAssets.count { it.type == "audio" }
                val admVideos = filteredAdminAssets.count { it.type == "video" }

                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary)),
                    isDark = isDark,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛡️ Admin Uploaded Media (${filteredAdminAssets.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "🖼️ $admPhotos Photos • 🎵 $admAudios Audios • 🎬 $admVideos Videos (Storefront, BGM & Template Slots)",
                                fontSize = 10.sp,
                                color = mutedColor
                            )
                        }
                        ClayPill(text = "ADMIN VAULT", color = ClayColors.Emerald, isDark = isDark)
                    }
                }
            }

            if (filteredAdminAssets.isEmpty()) {
                item {
                    ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No Admin-uploaded media matches the current filter.",
                            fontSize = 11.sp,
                            color = mutedColor
                        )
                    }
                }
            } else {
                items(filteredAdminAssets, key = { "adm_${it.id}" }) { item ->
                    RenderMediaAssetCard(item = item, isUserUpload = false)
                }
            }
        }

        // =====================================================================
        // SECTION 2: 👤 USER UPLOADED MEDIA (COMPLETELY SEPARATE SECTION)
        // =====================================================================
        if (selectedUploaderSection == "both" || selectedUploaderSection == "user") {
            item {
                val usrPhotos = filteredUserAssets.count { it.type == "image" }
                val usrAudios = filteredUserAssets.count { it.type == "audio" }
                val usrVideos = filteredUserAssets.count { it.type == "video" }

                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Amber)),
                    isDark = isDark,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👤 User Uploaded Media (${filteredUserAssets.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "🖼️ $usrPhotos Photos • 🎵 $usrAudios Voice/Audios • 🎬 $usrVideos Videos • ${distinctUsers.size} Users",
                                fontSize = 10.sp,
                                color = mutedColor
                            )
                        }
                        if (selectedUserFilter != "all") {
                            ClayButton(
                                onClick = { selectedUserFilter = "all" },
                                variant = ClayButtonVariant.SECONDARY,
                                isDark = isDark
                            ) {
                                Text("✕ Clear User", fontSize = 9.5.sp, maxLines = 1)
                            }
                        } else {
                            ClayPill(text = "CUSTOMER VAULT", color = ClayColors.Pink, isDark = isDark)
                        }
                    }
                }
            }

            if (filteredUserAssets.isEmpty()) {
                item {
                    ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No User-uploaded media matches the current filter.",
                            fontSize = 11.sp,
                            color = mutedColor
                        )
                    }
                }
            } else {
                items(filteredUserAssets, key = { "usr_${it.id}" }) { item ->
                    RenderMediaAssetCard(item = item, isUserUpload = true)
                }
            }
        }
    }

    // Replace Template Image Slot Dialog
    replacingTemplateAsset?.let { slotItem ->
        var newImgUrl by remember(slotItem) { mutableStateOf(slotItem.url) }
        var newImgLabel by remember(slotItem) { mutableStateOf(slotItem.title) }
        Dialog(onDismissRequest = { replacingTemplateAsset = null }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary)),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Replace Template Image Slot (ImgBB)", fontSize = 15.sp, fontWeight = FontWeight.Black, color = textColor)
                    Text("Asset ID: ${slotItem.id} • Template: ${slotItem.templateName.ifBlank { "Global" }}", fontSize = 10.5.sp, color = mutedColor)
                    ClayTextField(value = newImgLabel, onValueChange = { newImgLabel = it }, label = "Slot Title / Alt Text", isDark = isDark)
                    ClayTextField(value = newImgUrl, onValueChange = { newImgUrl = it }, label = "New Image CDN URL", isDark = isDark)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { replacingTemplateAsset = null }) { Text("Cancel", color = mutedColor) }
                        Spacer(Modifier.width(8.dp))
                        ClayButton(
                            onClick = {
                                if (newImgUrl.isNotBlank()) {
                                    viewModel.replaceTemplateImageAsset(slotItem.id, newImgUrl, newImgLabel)
                                    replacingTemplateAsset = null
                                }
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Text("Replace Image", fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    // Full-Screen Image Lightbox Dialog
    lightboxAsset?.let { img ->
        Dialog(onDismissRequest = { lightboxAsset = null }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
                isDark = isDark
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = img.title,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        CompactIconButton(onClick = { lightboxAsset = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedColor)
                        }
                    }
                    AsyncImage(
                        model = img.url.ifBlank { img.thumbnailUrl },
                        contentDescription = img.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 360.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                    )
                    Text(
                        text = if (img.uploaderRole == "user") "👤 Uploaded by ${img.userName} (${img.userEmail})" else "🛡️ Admin Asset • ${img.provider} • ${img.sizeFormatted}",
                        fontSize = 10.5.sp,
                        color = mutedColor
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClayButton(
                            onClick = {
                                viewModel.setStorefrontHeroImage(img.rawUrl.ifBlank { img.url }, img.title)
                                lightboxAsset = null
                            },
                            variant = ClayButtonVariant.AMBER,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🌟 Set as Hero", fontSize = 10.5.sp, maxLines = 1)
                        }
                        ClayButton(
                            onClick = {
                                copyTextToClipboard(context, img.title, img.url)
                                viewModel.showToast("📋 Copied URL!", img.url)
                            },
                            variant = ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Text("Copy Link", fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

/**
 * DEDICATED NOTIFICATIONS & ALERTS SECTION (`NotificationsScreen` in Bottom Bar Corner)
 */
@Composable
fun NotificationsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    var selectedTab by remember { mutableStateOf("all") }
    var showBroadcastDialog by remember { mutableStateOf(false) }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted
    val unreadCount = notifications.count { !it.isRead }

    val filtered = notifications.filter { n ->
        when (selectedTab) {
            "unread" -> !n.isRead
            "wish" -> n.type.equals("wish", true)
            "order" -> n.type.equals("order", true)
            "support" -> n.type.equals("support", true)
            "festival" -> n.type.equals("festival", true)
            "broadcast" -> n.type.equals("broadcast", true)
            "template" -> n.type.equals("template", true) || n.type.equals("system", true)
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Live D1 Alerts Sync",
                titlePrefix = "Alerts &",
                pinkAccentWord = "Notifications",
                titleSuffix = "(${unreadCount} Unread / ${notifications.size})",
                primaryButtonText = "↻ Sync Live",
                primaryButtonIcon = Icons.Default.Sync,
                onPrimaryClick = { viewModel.syncLive() },
                secondaryButtonText = "✓ Mark All Read",
                secondaryButtonIcon = Icons.Default.DoneAll,
                onSecondaryClick = { viewModel.markAllNotificationsRead() },
                tertiaryButtonText = "📢 + Broadcast",
                onTertiaryClick = { showBroadcastDialog = true }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "all" to "All (${notifications.size})",
                    "unread" to "🔴 Unread ($unreadCount)",
                    "wish" to "🎁 Wishes",
                    "order" to "💳 Orders",
                    "support" to "🎫 Support & Mod",
                    "festival" to "🪔 Festivals",
                    "broadcast" to "📢 Broadcasts",
                    "template" to "🎨 System"
                ).forEach { (key, label) ->
                    ClayButton(
                        onClick = { selectedTab = key },
                        variant = if (selectedTab == key) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Text(label, fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                TopAccentCard(
                    accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
                    isDark = isDark
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
                    ) {
                        Text("🔔 All Caught Up!", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                        Text("Live wish creations, orders, support tickets, festival alerts, and D1 broadcasts sync here automatically.", fontSize = 11.sp, color = mutedColor)
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { notif ->
            val pillColor = when (notif.type) {
                "festival" -> ClayColors.Amber
                "order" -> ClayColors.Emerald
                "support" -> ClayColors.Rose
                "broadcast" -> ClayColors.Sky
                "wish" -> ClayColors.Pink
                else -> ClayColors.Primary
            }
            TopAccentCard(
                accentBrush = when (notif.type) {
                    "festival" -> Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink))
                    "order" -> Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Sky))
                    "support" -> Brush.horizontalGradient(listOf(ClayColors.Rose, ClayColors.Pink))
                    "wish" -> Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary))
                    else -> Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Emerald))
                },
                isDark = isDark,
                contentPadding = PaddingValues(11.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (!notif.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ClayColors.Pink)
                                )
                            }
                            ClayPill(
                                text = notif.type.uppercase(),
                                color = pillColor,
                                isDark = isDark
                            )
                            if (!notif.isRead) {
                                ClayPill(text = "NEW", color = ClayColors.Rose, isDark = isDark)
                            }
                        }
                        Text(notif.timeAgo, fontSize = 9.5.sp, color = mutedColor, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = notif.title,
                        fontSize = 13.sp,
                        fontWeight = if (!notif.isRead) FontWeight.Black else FontWeight.Bold,
                        color = textColor
                    )
                    Text(notif.message, fontSize = 11.sp, color = mutedColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val targetSection = when (notif.type) {
                            "festival" -> AdminSection.FESTIVALS
                            "wish" -> AdminSection.WISHES
                            "order" -> AdminSection.ORDERS
                            "support" -> AdminSection.SUPPORT
                            "broadcast" -> AdminSection.SETTINGS
                            else -> AdminSection.TEMPLATES
                        }
                        ClayButton(
                            onClick = {
                                if (!notif.isRead) viewModel.markNotificationRead(notif.id)
                                viewModel.selectSection(targetSection)
                            },
                            variant = ClayButtonVariant.PRIMARY,
                            isDark = isDark
                        ) {
                            Text("Open ${targetSection.shortLabel}", fontSize = 10.sp, maxLines = 1)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!notif.isRead) {
                                CompactIconButton(
                                    onClick = { viewModel.markNotificationRead(notif.id) },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ClayColors.Emerald.copy(alpha = 0.15f))
                                        .border(1.dp, ClayColors.Emerald.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Mark Read", tint = ClayColors.Emerald, modifier = Modifier.size(15.dp))
                                }
                            }
                            CompactIconButton(
                                onClick = { viewModel.deleteNotification(notif.id) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ClayColors.Rose.copy(alpha = 0.15f))
                                    .border(1.dp, ClayColors.Rose.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Dismiss", tint = ClayColors.Rose, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBroadcastDialog) {
        var bTitle by remember { mutableStateOf("") }
        var bMessage by remember { mutableStateOf("") }
        var bAudience by remember { mutableStateOf("all_users") }
        Dialog(onDismissRequest = { showBroadcastDialog = false }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)),
                isDark = isDark,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📢 Send Live Notification Broadcast", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                        CompactIconButton(onClick = { showBroadcastDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedColor, modifier = Modifier.size(16.dp))
                        }
                    }
                    ClayTextField(
                        value = bTitle,
                        onValueChange = { bTitle = it },
                        label = "Notification Title",
                        placeholder = "e.g., Diwali Special Templates Now Live!",
                        isDark = isDark
                    )
                    ClayTextField(
                        value = bMessage,
                        onValueChange = { bMessage = it },
                        label = "Message Body",
                        placeholder = "Create a magical celebration wish for your loved ones...",
                        singleLine = false,
                        isDark = isDark
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ClayButton(
                            onClick = { showBroadcastDialog = false },
                            variant = ClayButtonVariant.SECONDARY,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 11.sp)
                        }
                        ClayButton(
                            onClick = {
                                if (bTitle.isNotBlank() && bMessage.isNotBlank()) {
                                    viewModel.sendBroadcastNotification(bTitle.trim(), bMessage.trim(), bAudience)
                                    showBroadcastDialog = false
                                }
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send to Live D1", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val analytics by viewModel.analytics.collectAsState()

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Real-Time Telemetry",
                titlePrefix = "Analytics &",
                pinkAccentWord = "Growth",
                titleSuffix = "Studio",
                primaryButtonText = "↻ Refresh",
                primaryButtonIcon = Icons.Default.Refresh,
                onPrimaryClick = { viewModel.syncLive() }
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SparklineKpiCard(
                    title = "Total Revenue",
                    value = "₹${analytics.totalRevenueInr}",
                    subtitle = "${analytics.paidOrdersCount} Paid Orders",
                    symbolText = "₹",
                    iconBrush = Brush.linearGradient(listOf(ClayColors.EmeraldLight, ClayColors.Emerald)),
                    sparklineColor = ClayColors.Emerald,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                SparklineKpiCard(
                    title = "Total Views",
                    value = "${analytics.totalViewsCount}",
                    subtitle = "${analytics.conversionRatePercent}% Funnel",
                    icon = Icons.Default.Visibility,
                    iconBrush = Brush.linearGradient(listOf(ClayColors.PinkLight, ClayColors.Pink)),
                    sparklineColor = ClayColors.Pink,
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Executive Telemetry Snapshot", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    Text("• Top Performing Template: ${analytics.topTemplateName}", fontSize = 11.5.sp, color = mutedColor)
                    Text("• Active Wishes: ${analytics.activeWishesCount} • Upcoming: ${analytics.upcomingWishesCount} • Expired: ${analytics.expiredWishesCount}", fontSize = 11.5.sp, color = mutedColor)
                    Text("• Active VIP Subscribers: ${analytics.activeVipSubscribers} • Total Shares: ${analytics.totalSharesCount}", fontSize = 11.5.sp, color = mutedColor)
                }
            }
        }
    }
}

@Composable
fun StorageHealthScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val health by viewModel.storageHealth.collectAsState()
    val mediaSummary by viewModel.mediaSummary.collectAsState()
    val pingMs by viewModel.pingMs.collectAsState()

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "D1 + B2 + ImgBB",
                titlePrefix = "Storage",
                pinkAccentWord = "Health",
                titleSuffix = "& Backups",
                primaryButtonText = "⚡ Verify & Clean",
                primaryButtonIcon = Icons.Default.Verified,
                onPrimaryClick = { viewModel.runStorageVerifyAndCleanup() },
                secondaryButtonText = "🔧 Reconcile B2",
                secondaryButtonIcon = Icons.Default.Build,
                onSecondaryClick = { viewModel.healAndReconcileMedia() }
            )
        }

        item {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadgePill(text = "ALL CLOUD SYSTEMS OPERATIONAL", isSuccess = true, isDark = isDark)
                    Text("• Cloudflare D1 Database (wishmagic-db): ${health.d1Status} (${pingMs}ms)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text("• Backblaze B2 Bucket (wishmagic-assets): ${health.b2Status} (${mediaSummary.b2CloudCount} B2 objects • ${mediaSummary.audioCount} audio tracks)", fontSize = 11.5.sp, color = mutedColor)
                    Text("• ImgBB Image CDN: ${health.imgbbStatus} (${mediaSummary.imgbbCount} ImgBB images • ${mediaSummary.templateImagesCount} template slots)", fontSize = 11.5.sp, color = mutedColor)
                    Text("• Untracked Cloud Objects: ${mediaSummary.untrackedCloudCount} • Quarantine Queue: ${health.quarantineCount}", fontSize = 11.sp, color = mutedColor)
                }
            }
        }

        item {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Amber)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💾 Full Platform Snapshot & JSON Backup", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                    Text("Export complete site settings, templates, and media catalog as a portable JSON snapshot.", fontSize = 11.sp, color = mutedColor)
                    ClayButton(
                        onClick = {
                            val json = viewModel.repository.exportBackupJson()
                            copyTextToClipboard(context, "WishMagic Backup JSON", json)
                            viewModel.showToast("💾 Snapshot Copied!", "Full D1 & Settings JSON backup copied to clipboard.")
                        },
                        variant = ClayButtonVariant.PINK,
                        isDark = isDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy Backup JSON", fontSize = 10.5.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val logs by viewModel.auditLogs.collectAsState()

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Immutable Security Trail",
                titlePrefix = "Activity &",
                pinkAccentWord = "Audit Logs",
                titleSuffix = "(${logs.size})"
            )
        }

        items(logs, key = { it.id }) { log ->
            ClayCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ClayPill(text = log.action, color = ClayColors.Pink, isDark = isDark)
                        Text(log.createdAt, fontSize = 9.5.sp, color = mutedColor)
                    }
                    Text(log.target, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    Text("${log.details} • Actor: ${log.actorEmail}", fontSize = 10.5.sp, color = mutedColor)
                }
            }
        }
    }
}

@Composable
fun SupportTicketsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val tickets by viewModel.supportTickets.collectAsState()
    var slugToRepair by remember { mutableStateOf("") }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Support & Link Recovery",
                titlePrefix = "Support &",
                pinkAccentWord = "Link Repair",
                titleSuffix = "Console"
            )
        }

        item {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🛠️ 1-Click Celebration Link Auto-Repair", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                    ClayTextField(value = slugToRepair, onValueChange = { slugToRepair = it }, label = "WISH SLUG TO REPAIR", placeholder = "rahul-diwali-2026", isDark = isDark)
                    GradientPillButton(
                        text = "Auto-Repair Link & Extend +7 Days",
                        icon = Icons.Default.Build,
                        onClick = {
                            if (slugToRepair.isNotBlank()) {
                                viewModel.repairWishLink(slugToRepair)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        brush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary))
                    )
                }
            }
        }

        items(tickets, key = { it.id }) { t ->
            val resolved = t.status.equals("resolved", true)
            TopAccentCard(
                accentBrush = if (resolved) Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Sky))
                else Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink)),
                isDark = isDark
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatusBadgePill(text = t.status.uppercase(), isSuccess = resolved, isDark = isDark)
                        ClayPill(text = t.priority.uppercase(), color = ClayColors.Pink, isDark = isDark)
                    }
                    Text(t.subject, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    Text("${t.customerEmail} • Wish: /w/${t.wishSlug}", fontSize = 11.sp, color = mutedColor)
                    Text(t.message, fontSize = 11.5.sp, color = textColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (t.wishSlug.isNotBlank()) {
                            ClayButton(
                                onClick = { viewModel.repairWishLink(t.wishSlug) },
                                variant = ClayButtonVariant.EMERALD,
                                isDark = isDark
                            ) {
                                Text("Repair /w/${t.wishSlug}", fontSize = 10.5.sp, maxLines = 1)
                            }
                        }
                        if (!resolved) {
                            ClayButton(
                                onClick = { viewModel.resolveTicket(t.id) },
                                variant = ClayButtonVariant.PRIMARY,
                                isDark = isDark
                            ) {
                                Text("Mark Resolved", fontSize = 10.5.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModerationQueueScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val queue by viewModel.moderationQueue.collectAsState()

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "Trust & Safety",
                titlePrefix = "Moderation",
                pinkAccentWord = "Queue",
                titleSuffix = "(${queue.size})",
                primaryButtonText = "↻ Refresh",
                primaryButtonIcon = Icons.Default.Refresh,
                onPrimaryClick = { viewModel.syncLive() }
            )
        }

        items(queue, key = { it.id }) { item ->
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Rose)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Target: /w/${item.wishSlug}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                        ClayPill(text = item.status.uppercase(), color = ClayColors.Amber, isDark = isDark)
                    }
                    Text("Reason: ${item.reason} • By: ${item.submittedBy}", fontSize = 11.sp, color = mutedColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClayButton(
                            onClick = { viewModel.moderateWishItem(item.id, "approved") },
                            variant = ClayButtonVariant.EMERALD,
                            isDark = isDark
                        ) {
                            Text("Approve", fontSize = 10.5.sp, maxLines = 1)
                        }
                        ClayButton(
                            onClick = { viewModel.moderateWishItem(item.id, "rejected") },
                            variant = ClayButtonVariant.DANGER,
                            isDark = isDark
                        ) {
                            Text("Reject & Suspend", fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralsStudioScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val pricing by viewModel.pricingConfig.collectAsState()
    val users by viewModel.users.collectAsState()

    var grantTargetEmail by remember { mutableStateOf("") }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = "${pricing.referralRequiredCount}-Friends Milestone",
                titlePrefix = "Referrals &",
                pinkAccentWord = "Free Link",
                titleSuffix = "Rewards"
            )
        }

        item {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Pink)), isDark = isDark) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎁 Grant Free Custom Link Credit (Worth ₹${pricing.customSlugPrice})", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                    ClayTextField(value = grantTargetEmail, onValueChange = { grantTargetEmail = it }, label = "USER EMAIL OR ID", placeholder = "creator@gmail.com", isDark = isDark)
                    GradientPillButton(
                        text = "+ Grant 1 Free Custom Link Credit",
                        icon = Icons.Default.CardGiftcard,
                        onClick = {
                            if (grantTargetEmail.isNotBlank()) {
                                viewModel.grantReferralCredit(grantTargetEmail, 1)
                                grantTargetEmail = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        items(users, key = { it.id }) { u ->
            ClayCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(u.displayName, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                        Text("${u.email} • Referrals: ${u.referralsCount}/${pricing.referralRequiredCount}", fontSize = 10.5.sp, color = mutedColor)
                    }
                    ClayButton(
                        onClick = { viewModel.grantReferralCredit(u.email, 1) },
                        variant = ClayButtonVariant.PINK,
                        isDark = isDark
                    ) {
                        Text("+1 Free Link", fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun SpecializedStudioBridgeScreen(
    viewModel: AdminViewModel,
    section: AdminSection,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val staffMembers by viewModel.staffMembers.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val searchSynonyms by viewModel.searchSynonyms.collectAsState()
    val userCreations by viewModel.userCreations.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    var previewPhotoUrl by remember { mutableStateOf<String?>(null) }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            CosmicHeroBanner(
                welcomePillText = section.pill,
                titlePrefix = section.shortLabel,
                pinkAccentWord = "Studio",
                titleSuffix = "Console",
                primaryButtonText = "↻ Sync D1",
                primaryButtonIcon = Icons.Default.Refresh,
                onPrimaryClick = { viewModel.syncLive() }
            )
        }

        when (section) {
            AdminSection.STAFF -> {
                item {
                    var name by remember { mutableStateOf("") }
                    var mobile by remember { mutableStateOf("") }
                    var role by remember { mutableStateOf("ADMIN") }
                    var pass by remember { mutableStateOf("WishMagic@2026") }

                    TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)), isDark = isDark) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Add Staff Member (RBAC)", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                            ClayTextField(value = name, onValueChange = { name = it }, label = "Staff Name", placeholder = "Rohan Sharma", isDark = isDark)
                            ClayTextField(value = mobile, onValueChange = { mobile = it }, label = "Mobile / Identifier", placeholder = "+919876543210", isDark = isDark)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ClayTextField(value = role, onValueChange = { role = it }, label = "Role", isDark = isDark)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ClayTextField(value = pass, onValueChange = { pass = it }, label = "Password", isDark = isDark)
                                }
                            }
                            ClayButton(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        viewModel.addStaffMember(name, mobile, role, pass)
                                        name = ""
                                        mobile = ""
                                    }
                                },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark
                            ) {
                                Text("+ Add Staff Member", fontSize = 10.5.sp, maxLines = 1)
                            }
                        }
                    }
                }

                items(staffMembers, key = { it.id }) { st ->
                    ClayCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(st.name, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                                    ClayPill(text = st.role, color = ClayColors.Pink, isDark = isDark)
                                }
                                Text("${st.mobile} • Status: ${st.status}", fontSize = 10.5.sp, color = mutedColor)
                            }
                            CompactIconButton(onClick = { viewModel.deleteStaffMember(st.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ClayColors.Rose, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            AdminSection.ANNOUNCEMENTS -> {
                item {
                    var title by remember { mutableStateOf("") }
                    var msg by remember { mutableStateOf("") }

                    TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink)), isDark = isDark) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Broadcast Global Announcement", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                            ClayTextField(value = title, onValueChange = { title = it }, label = "Headline", placeholder = "🪔 Diwali Special Templates Live!", isDark = isDark)
                            ClayTextField(value = msg, onValueChange = { msg = it }, label = "Message Body", placeholder = "Create your personalized 3D celebration wish!", isDark = isDark)
                            ClayButton(
                                onClick = {
                                    if (title.isNotBlank()) {
                                        viewModel.createAndPublishAnnouncement(title, msg)
                                        title = ""
                                        msg = ""
                                    }
                                },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark
                            ) {
                                Text("📣 Publish Announcement", fontSize = 10.5.sp, maxLines = 1)
                            }
                        }
                    }
                }

                items(announcements, key = { it.id }) { ann ->
                    ClayCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(ann.title, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                                StatusBadgePill(text = ann.status.uppercase(), isSuccess = true, isDark = isDark)
                            }
                            Text(ann.message, fontSize = 11.sp, color = mutedColor)
                        }
                    }
                }
            }

            AdminSection.SEARCH -> {
                item {
                    var term by remember { mutableStateOf("") }
                    var syns by remember { mutableStateOf("") }

                    TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Sky)), isDark = isDark) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Search Synonyms Studio", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                                ClayButton(
                                    onClick = { viewModel.rebuildSearchIndex() },
                                    variant = ClayButtonVariant.EMERALD,
                                    isDark = isDark
                                ) {
                                    Text("⚡ Rebuild Index", fontSize = 10.sp, maxLines = 1)
                                }
                            }
                            ClayTextField(value = term, onValueChange = { term = it }, label = "Search Keyword", placeholder = "diwali", isDark = isDark)
                            ClayTextField(value = syns, onValueChange = { syns = it }, label = "Synonyms (comma separated)", placeholder = "deepavali, festival of lights", isDark = isDark)
                            ClayButton(
                                onClick = {
                                    if (term.isNotBlank() && syns.isNotBlank()) {
                                        viewModel.addSearchSynonym(term, syns)
                                        term = ""
                                        syns = ""
                                    }
                                },
                                variant = ClayButtonVariant.PINK,
                                isDark = isDark
                            ) {
                                Text("+ Save Synonym Rule", fontSize = 10.5.sp, maxLines = 1)
                            }
                        }
                    }
                }

                items(searchSynonyms, key = { it.id }) { s ->
                    ClayCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Keyword: \"${s.term}\"", fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                                Text("Synonyms: ${s.synonyms.joinToString(", ")}", fontSize = 11.sp, color = mutedColor)
                            }
                            CompactIconButton(onClick = { viewModel.deleteSearchSynonym(s.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ClayColors.Rose, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            AdminSection.USER_CREATIONS -> {
                items(userCreations, key = { it.id }) { uc ->
                    TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)), isDark = isDark) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadgePill(text = uc.status, isSuccess = true, isDark = isDark)
                                    ClayPill(text = "📷 ${uc.photosCount} Photos", color = ClayColors.Primary, isDark = isDark)
                                    if (uc.hasVoiceNote) {
                                        ClayPill(text = "🎙 Voice", color = ClayColors.Pink, isDark = isDark)
                                    }
                                    if (uc.hasVideo) {
                                        ClayPill(text = "🎬 Video", color = ClayColors.Amber, isDark = isDark)
                                    }
                                }
                            }
                            Text("For ${uc.recipientName} — By ${uc.userName} (${uc.userEmail})", fontSize = 14.sp, fontWeight = FontWeight.Black, color = textColor)
                            Text("Link: /w/${uc.slug} • Template: ${uc.templateName} • ${uc.createdAt}", fontSize = 10.5.sp, color = mutedColor)
                            if (uc.customMessage.isNotBlank()) {
                                Text("\"${uc.customMessage}\"", fontSize = 11.sp, color = textColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }

                            if (uc.photoUrls.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uc.photoUrls.forEach { pUrl ->
                                        AsyncImage(
                                            model = pUrl,
                                            contentDescription = "Uploaded Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ClayColors.Primary.copy(alpha = 0.15f))
                                                .clickable { previewPhotoUrl = pUrl }
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ClayButton(
                                    onClick = {
                                        val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$base/w/${uc.slug}")))
                                        }
                                    },
                                    variant = ClayButtonVariant.PINK,
                                    isDark = isDark
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open /w/${uc.slug}", fontSize = 10.sp, maxLines = 1)
                                }
                                if (uc.voiceNoteUrl.isNotBlank()) {
                                    ClayButton(
                                        onClick = {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uc.voiceNoteUrl)))
                                            }
                                        },
                                        variant = ClayButtonVariant.AMBER,
                                        isDark = isDark
                                    ) {
                                        Text("🎙 Play Voice", fontSize = 10.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                item {
                    TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)), isDark = isDark) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(section.title, fontSize = 15.sp, fontWeight = FontWeight.Black, color = textColor)
                            Text(section.desc, fontSize = 11.5.sp, color = mutedColor)
                            GradientPillButton(
                                text = "Launch ${section.shortLabel} Web Studio",
                                icon = Icons.Default.OpenInBrowser,
                                onClick = { viewModel.openFullStudioFor(section) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    previewPhotoUrl?.let { url ->
        Dialog(onDismissRequest = { previewPhotoUrl = null }) {
            TopAccentCard(accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)), isDark = isDark) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Customer Photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 380.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    ClayButton(
                        onClick = { previewPhotoUrl = null },
                        variant = ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Text("Close Preview", fontSize = 10.5.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}
