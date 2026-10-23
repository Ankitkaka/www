package com.example.ui.screens

import android.content.Intent
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

/**
 * Website-Matched Templates Library & Full-Page Template Editor Studio
 * - Shows ONLY the relationship variants that were actually created for each specific template!
 * - Uses compact icon-only buttons with crisp rounded corners and zero overlap.
 */
@Composable
fun TemplatesScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var editingTemplateId by remember { mutableStateOf<String?>(null) }
    var variantsModalTemplateId by remember { mutableStateOf<String?>(null) }
    var versionTemplateId by remember { mutableStateOf<String?>(null) }
    var previewModalTemplateId by remember { mutableStateOf<String?>(null) }
    var showQuickAddModal by remember { mutableStateOf(false) }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    val categories = listOf("All", "Festival", "Birthday", "Anniversary", "Love", "Friendship", "Celebration")
    val filtered = templates.filter { tpl ->
        val matchCat = selectedCategory == "All" || tpl.category.equals(selectedCategory, ignoreCase = true)
        val matchSearch = searchQuery.isBlank() ||
            tpl.name.contains(searchQuery, ignoreCase = true) ||
            tpl.slug.contains(searchQuery, ignoreCase = true) ||
            tpl.category.contains(searchQuery, ignoreCase = true) ||
            tpl.enabledVariants.any { it.contains(searchQuery, ignoreCase = true) }
        matchCat && matchSearch
    }

    val activeEditingTemplate = editingTemplateId?.let { targetId ->
        templates.firstOrNull { it.id == targetId }
    }

    if (activeEditingTemplate != null) {
        FullPageTemplateEditorScreen(
            template = activeEditingTemplate,
            isDark = isDark,
            serverUrl = serverUrl,
            modifier = modifier,
            onBack = { editingTemplateId = null },
            onOpenVariantsModal = {
                viewModel.loadTemplateFullConfig(activeEditingTemplate.id)
                variantsModalTemplateId = activeEditingTemplate.id
            },
            onOpenVersionsModal = {
                viewModel.loadTemplateFullConfig(activeEditingTemplate.id)
                versionTemplateId = activeEditingTemplate.id
            },
            onUploadCoverFromPhone = { uri, onUploaded ->
                viewModel.uploadTemplateCoverFromUri(context, activeEditingTemplate.id, uri, onUploaded)
            },
            onResetDefaults = { scope ->
                viewModel.resetTemplateConfigToDefaults(activeEditingTemplate.id, scope)
            },
            onSave = { name, slug, category, description, thumbnail, capabilities, price, tier, isPub, isFeat, prio, enabledVars, secs, acts, varsList, festMeta, scope, publishNow ->
                viewModel.updateTemplateFullCapabilities(
                    templateId = activeEditingTemplate.id,
                    price = price,
                    tier = tier,
                    isPublished = isPub,
                    isFeatured = isFeat,
                    priority = prio,
                    enabledVariants = enabledVars,
                    sections = secs,
                    actions = acts,
                    variantsList = varsList,
                    festivalMetadata = festMeta,
                    scope = scope,
                    publishNow = publishNow,
                    name = name,
                    slug = slug,
                    category = category,
                    description = description,
                    thumbnail = thumbnail,
                    capabilities = capabilities
                )
                if (publishNow) editingTemplateId = null
            }
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                CosmicHeroBanner(
                    welcomePillText = "UTP v1.4.0 Studio",
                    titlePrefix = "Celebration",
                    pinkAccentWord = "Templates",
                    titleSuffix = "(${filtered.size})",
                    primaryButtonText = "+ Upload ZIP",
                    primaryButtonIcon = Icons.Default.UploadFile,
                    onPrimaryClick = { viewModel.selectSection(AdminSection.ADD_TEMPLATE) },
                    secondaryButtonText = "+ Quick Add",
                    secondaryButtonIcon = Icons.Default.AddCircleOutline,
                    onSecondaryClick = { showQuickAddModal = true }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val active = selectedCategory == cat
                        ClayButton(
                            onClick = { selectedCategory = cat },
                            variant = if (active) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Text(cat, fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }
            }

            items(filtered, key = { it.id }) { tpl ->
                val isVip = tpl.price > 0 || tpl.tier.equals("vip", ignoreCase = true)
                val tplActualVariants = remember(tpl.id, tpl.variantsList, tpl.enabledVariants) {
                    (tpl.variantsList.map { it.relationship } + tpl.enabledVariants)
                        .map { it.trim().lowercase() }
                        .filter { it.isNotBlank() }
                        .distinct()
                }
                TopAccentCard(
                    accentBrush = if (isVip) {
                        Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink))
                    } else {
                        Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink))
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
                                AsyncImage(
                                    model = tpl.thumbnail,
                                    contentDescription = tpl.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(ClayColors.Primary.copy(alpha = 0.15f))
                                        .clickable {
                                            viewModel.loadTemplateFullConfig(tpl.id)
                                            editingTemplateId = tpl.id
                                        }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ClayPill(
                                            text = if (isVip) "VIP ₹${tpl.price.toInt()}" else "FREE",
                                            color = if (isVip) ClayColors.Amber else ClayColors.Emerald,
                                            isDark = isDark
                                        )
                                        ClayPill(
                                            text = tpl.category,
                                            color = ClayColors.Primary,
                                            isDark = isDark
                                        )
                                        if (tpl.isFeatured) {
                                            ClayPill(text = "★", color = ClayColors.Amber, isDark = isDark)
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = tpl.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val varSummary = if (tplActualVariants.isNotEmpty()) {
                                        "${tplActualVariants.size} Variants (${tplActualVariants.take(3).joinToString(", ")})"
                                    } else {
                                        "Single Edition"
                                    }
                                    Text(
                                        text = "$varSummary • v${tpl.templateVersion}",
                                        fontSize = 10.sp,
                                        color = mutedColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            ClaySwitch(
                                checked = tpl.isPublished,
                                onCheckedChange = { viewModel.toggleTemplatePublished(tpl.id, it) },
                                isDark = isDark
                            )
                        }

                        // Compact Icon-Only Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Edit Full-Page Studio (Compact Icon Box)
                                CompactIconButton(
                                    onClick = {
                                        viewModel.loadTemplateFullConfig(tpl.id)
                                        editingTemplateId = tpl.id
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)))
                                        .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Template",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // 2. Relationship Variants Manager (Icon + Actual Variant Count)
                                Row(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ClayColors.Primary.copy(alpha = 0.14f))
                                        .border(1.dp, ClayColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.loadTemplateFullConfig(tpl.id)
                                            variantsModalTemplateId = tpl.id
                                        }
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = "Variants",
                                        tint = ClayColors.Primary,
                                        modifier = Modifier.size(13.5.dp)
                                    )
                                    Text(
                                        text = "${tplActualVariants.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ClayColors.Primary
                                    )
                                }

                                // 3. Version History (Compact Icon Box)
                                CompactIconButton(
                                    onClick = {
                                        viewModel.loadTemplateFullConfig(tpl.id)
                                        versionTemplateId = tpl.id
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ClayColors.Sky.copy(alpha = 0.14f))
                                        .border(1.dp, ClayColors.Sky.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Version History",
                                        tint = ClayColors.Sky,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // 4. In-App Live Preview (Compact Icon Box)
                                CompactIconButton(
                                    onClick = {
                                        viewModel.loadTemplateFullConfig(tpl.id)
                                        previewModalTemplateId = tpl.id
                                    },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ClayColors.Emerald.copy(alpha = 0.14f))
                                        .border(1.dp, ClayColors.Emerald.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Preview",
                                        tint = ClayColors.Emerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // 5. Duplicate Template (Compact Icon Box)
                                CompactIconButton(
                                    onClick = { viewModel.duplicateTemplate(tpl.id, tpl.name) },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ClayColors.Amber.copy(alpha = 0.14f))
                                        .border(1.dp, ClayColors.Amber.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate",
                                        tint = ClayColors.Amber,
                                        modifier = Modifier.size(13.5.dp)
                                    )
                                }
                            }

                            // 6. Delete Template (Compact Icon Box)
                            CompactIconButton(
                                onClick = { viewModel.deleteTemplate(tpl.id, tpl.name) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ClayColors.Rose.copy(alpha = 0.14f))
                                    .border(1.dp, ClayColors.Rose.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = ClayColors.Rose,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: In-App Live Template Preview Dialog
    previewModalTemplateId?.let { targetId ->
        val tpl = templates.firstOrNull { it.id == targetId }
        if (tpl != null) {
            val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
            val liveUrl = "$base/template-preview.html?template=${tpl.slug}"
            FullscreenTemplatePreviewDialog(
                title = tpl.name,
                livePreviewUrl = liveUrl,
                variantsList = tpl.variantsList,
                actionsList = tpl.actions,
                fieldsList = tpl.fields,
                sectionsList = tpl.sections,
                isDark = isDark,
                onDismiss = { previewModalTemplateId = null }
            )
        }
    }

    // Modal: Relationship Variants Manager Modal
    variantsModalTemplateId?.let { targetId ->
        val tpl = templates.firstOrNull { it.id == targetId }
        if (tpl != null) {
            RelationshipVariantsManagerDialog(
                template = tpl,
                isDark = isDark,
                serverUrl = serverUrl,
                onDismiss = { variantsModalTemplateId = null },
                onSaveVariant = { variant ->
                    viewModel.saveTemplateVariant(tpl.id, variant)
                },
                onCloneVariant = { srcRel, targetRel, newName ->
                    viewModel.cloneTemplateVariant(tpl.id, srcRel, targetRel, newName)
                }
            )
        }
    }

    // Modal: Template Version Control Modal
    versionTemplateId?.let { targetId ->
        val tpl = templates.firstOrNull { it.id == targetId }
        if (tpl != null) {
            TemplateVersionControlDialog(
                template = tpl,
                isDark = isDark,
                onDismiss = { versionTemplateId = null },
                onUploadNewVersion = {
                    versionTemplateId = null
                    viewModel.selectSection(AdminSection.ADD_TEMPLATE)
                },
                onRollback = { verId -> viewModel.rollbackTemplateVersion(tpl.id, verId) },
                onPublishVersion = { verId -> viewModel.publishTemplateVersion(tpl.id, verId) },
                onValidateVersion = { verId -> viewModel.validateTemplateVersion(tpl.id, verId) },
                onPreview = {
                    val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                    val previewUrl = "$base/template-preview.html?template=${tpl.slug}"
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(previewUrl))) }
                }
            )
        }
    }

    if (showQuickAddModal) {
        var name by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("0") }
        var desc by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showQuickAddModal = false }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
                isDark = isDark,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quick Register Template", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    ClayTextField(value = name, onValueChange = { name = it }, label = "Template Name", placeholder = "Royal Diwali Lights", isDark = isDark)
                    ClayTextField(value = price, onValueChange = { price = it }, label = "Base Price (₹ INR)", placeholder = "0", isDark = isDark)
                    ClayTextField(value = desc, onValueChange = { desc = it }, label = "Description", placeholder = "Interactive 3D celebration", isDark = isDark)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showQuickAddModal = false }) { Text("Cancel", color = mutedColor) }
                        Spacer(Modifier.width(6.dp))
                        ClayButton(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val p = price.toDoubleOrNull() ?: 0.0
                                    viewModel.addNewTemplate(name, if (p > 0) "vip" else "free", p, desc)
                                    showQuickAddModal = false
                                }
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Text("Register", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * COMPACT 5-TAB FULL-PAGE TEMPLATE EDITOR SCREEN
 * Strictly displays ONLY the variants created for this specific template (`template.variantsList` + `template.enabledVariants`).
 */
@Composable
private fun FullPageTemplateEditorScreen(
    template: TemplateItem,
    isDark: Boolean,
    serverUrl: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenVariantsModal: () -> Unit,
    onOpenVersionsModal: () -> Unit,
    onUploadCoverFromPhone: (uri: Uri, onUploaded: (String) -> Unit) -> Unit,
    onResetDefaults: (scope: String) -> Unit,
    onSave: (
        name: String,
        slug: String,
        category: String,
        description: String,
        thumbnail: String,
        capabilities: List<String>,
        price: Double,
        tier: String,
        isPublished: Boolean,
        isFeatured: Boolean,
        priority: Int,
        enabledVariants: List<String>,
        sections: List<TemplateSectionNode>,
        actions: List<TemplateActionNode>,
        variantsList: List<TemplateVariantNode>,
        festivalMetadata: FestivalMetadataInfo,
        scope: String,
        publishNow: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    var activeTab by remember { mutableStateOf(0) }
    var showFullEditorPreview by remember { mutableStateOf(false) }
    val editorTabs = listOf(
        "🖼️ Cover & Info",
        "💰 Price & Caps",
        "👥 Variants",
        "🧩 Sections",
        "🔗 Links & Fest",
        "📱 Live Preview"
    )

    // 1. Basic Metadata & Cover Image State
    var nameText by remember(template.id) { mutableStateOf(template.name) }
    var slugText by remember(template.id) { mutableStateOf(template.slug) }
    var categoryText by remember(template.id) { mutableStateOf(template.category.ifBlank { "Celebration" }) }
    var descriptionText by remember(template.id) { mutableStateOf(template.description) }
    var thumbnailText by remember(template.id, template.thumbnail) { mutableStateOf(template.thumbnail) }
    var isUploadingCover by remember { mutableStateOf(false) }

    // 2. Pricing, Tier, Status, Priority & Featured State
    var priceText by remember(template.id) { mutableStateOf(template.price.toInt().toString()) }
    var tierMode by remember(template.id) {
        mutableStateOf(if (template.price > 0 || template.tier.equals("vip", true)) "vip" else "free")
    }
    var isPublished by remember(template.id) { mutableStateOf(template.isPublished) }
    var isFeatured by remember(template.id) { mutableStateOf(template.isFeatured) }
    var priorityText by remember(template.id) { mutableStateOf(template.placementPriority.toString()) }

    // 3. Capabilities Matrix State
    val allWebsiteCapabilities = remember {
        listOf(
            "hero" to "Hero Box",
            "gallery" to "Gallery",
            "message" to "Letter",
            "memories" to "Memories",
            "music" to "Music BGM",
            "voiceNote" to "Voice Note",
            "countdown" to "Countdown",
            "secretMessage" to "Secret Msg",
            "openWhen" to "Open When",
            "timeline" to "Timeline",
            "finalSurprise" to "Finale",
            "galleryLayouts" to "Layouts",
            "themeSupport" to "Themes",
            "animationSupport" to "3D FX",
            "backgroundSupport" to "Backdrops"
        )
    }
    var selectedCapabilities by remember(template.id) {
        val initial = (template.capabilities + listOf(
            "hero", "gallery", "message", "memories", "music", "voiceNote",
            "secretMessage", "finalSurprise", "galleryLayouts", "themeSupport",
            "animationSupport", "backgroundSupport"
        )).distinct()
        mutableStateOf(initial)
    }

    // 4. UTP Variants, Scope, Sections, Subsections & Logical Links State
    // ONLY use variants that actually belong to this template!
    var variantsList by remember(template.id, template.variantsList) { mutableStateOf(template.variantsList) }
    var enabledVariants by remember(template.id, template.enabledVariants, template.variantsList) {
        val actual = (template.enabledVariants + template.variantsList.filter { it.enabled }.map { it.relationship })
            .map { it.trim().lowercase().replace("-", "_").replace(" ", "_") }
            .filter { it.isNotBlank() }
            .distinct()
        mutableStateOf(actual)
    }
    var newVariantInput by remember { mutableStateOf("") }
    var sections by remember(template.id, template.sections) { mutableStateOf(template.sections) }
    var actions by remember(template.id, template.actions) { mutableStateOf(template.actions) }
    var selectedScope by remember { mutableStateOf("all") }

    // 5. UTP v1.4.0 Festival Metadata State
    var festivalTagsCsv by remember(template.id) { mutableStateOf(template.festivalMetadata.festivalTags.joinToString(", ")) }
    var festivalBadgeText by remember(template.id) { mutableStateOf(template.festivalMetadata.displayBadge) }
    var campaignCompatMode by remember(template.id) { mutableStateOf(template.festivalMetadata.campaignCompatibility) }
    var suggestedDaysBefore by remember(template.id) { mutableStateOf(template.festivalMetadata.suggestedDaysBefore.toString()) }
    var suggestedDaysAfter by remember(template.id) { mutableStateOf(template.festivalMetadata.suggestedDaysAfter.toString()) }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingCover = true
            onUploadCoverFromPhone(uri) { uploadedUrl ->
                thumbnailText = uploadedUrl
                isUploadingCover = false
            }
        }
    }

    // STRICTLY ONLY VARIANTS CREATED FOR THIS TEMPLATE (Never 18 hardcoded relationships!)
    val templateSpecificVariants = remember(template.id, variantsList, enabledVariants) {
        (variantsList.map { it.relationship } + enabledVariants)
            .map { it.trim().lowercase().replace("-", "_").replace(" ", "_") }
            .filter { it.isNotBlank() }
            .distinct()
    }

    val categoryOptions = listOf(
        "Festival", "Birthday", "Anniversary", "Love", "Friendship", "Celebration", "Wedding", "Thank You", "Congratulations"
    )
    val actionTypes = listOf("template_link", "global_link", "customizer", "section", "modal", "internal_url", "external_url", "none")
    val templateLogicalLinks = listOf("template.customize", "template.create", "template.preview")
    val globalLogicalLinks = listOf(
        "global.explore", "global.browseTemplates", "global.home", "global.createWish",
        "global.marketplace", "global.pricing", "global.help", "global.contact"
    )
    val campaignCompatOptions = listOf(
        "evergreen_and_festival", "festival_campaign", "festival_only", "any"
    )

    fun isSectionEnabledForActiveScope(sec: TemplateSectionNode, parentEnabled: Boolean = true): Boolean {
        if (!parentEnabled) return false
        if (sec.required) return true
        if (selectedScope == "all") return sec.enabled
        val vNode = variantsList.firstOrNull { it.relationship == selectedScope }
        return vNode?.visibilityOverrides?.get(sec.id) ?: sec.enabled
    }

    fun setSectionEnabledForActiveScope(secId: String, checked: Boolean) {
        if (selectedScope == "all") {
            sections = sections.map { top ->
                if (top.id == secId && !top.required) {
                    top.copy(enabled = checked)
                } else {
                    top.copy(
                        subsections = top.subsections.map { sub ->
                            if (sub.id == secId && !sub.required) sub.copy(enabled = checked) else sub
                        }
                    )
                }
            }
        } else {
            variantsList = variantsList.map { v ->
                if (v.relationship == selectedScope) {
                    val nextVis = v.visibilityOverrides.toMutableMap().apply { put(secId, checked) }
                    v.copy(visibilityOverrides = nextVis)
                } else v
            }.let { list ->
                if (list.any { it.relationship == selectedScope }) list
                else list + TemplateVariantNode(
                    id = "var_$selectedScope",
                    slug = selectedScope.replace("_", "-"),
                    name = "${selectedScope.replace("_", " ").replaceFirstChar { it.uppercase() }} Edition",
                    relationship = selectedScope,
                    relationshipLabel = selectedScope.replace("_", " ").replaceFirstChar { it.uppercase() },
                    visibilityOverrides = mapOf(secId to checked)
                )
            }
        }
    }

    fun triggerSave(publishNow: Boolean) {
        val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
        val effectiveTier = if (tierMode == "free" || parsedPrice <= 0.0) "free" else "vip"
        val effectivePrice = if (effectiveTier == "free") 0.0 else if (parsedPrice > 0.0) parsedPrice else 49.0
        val fm = FestivalMetadataInfo(
            festivalTags = festivalTagsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
            suitableRelationships = enabledVariants,
            campaignCompatibility = campaignCompatMode,
            displayBadge = festivalBadgeText,
            suggestedDaysBefore = suggestedDaysBefore.toIntOrNull() ?: 15,
            suggestedDaysAfter = suggestedDaysAfter.toIntOrNull() ?: 3
        )
        onSave(
            nameText.ifBlank { template.name },
            slugText.ifBlank { template.slug },
            categoryText.ifBlank { template.category },
            descriptionText,
            thumbnailText.ifBlank { template.thumbnail },
            selectedCapabilities,
            effectivePrice,
            effectiveTier,
            isPublished,
            isFeatured,
            priorityText.toIntOrNull() ?: 1,
            enabledVariants,
            sections,
            actions,
            variantsList,
            fm,
            selectedScope,
            publishNow
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. ULTRA-COMPACT STICKY TOP BAR + 5-TAB BAR
        TopAccentCard(
            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        CompactIconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Primary.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = ClayColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nameText.ifBlank { template.name },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${template.id} • ${if (tierMode == "vip") "VIP ₹$priceText" else "Free ₹0"} • v${template.schemaVersion}",
                                fontSize = 9.5.sp,
                                color = mutedColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Compact Icon Action Bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactIconButton(
                            onClick = {
                                val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                                val previewUrl = "$base/template-preview.html?template=${slugText.ifBlank { template.slug }}"
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(previewUrl))) }
                            },
                            modifier = Modifier
                                .size(31.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Emerald.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = ClayColors.Emerald, modifier = Modifier.size(15.dp))
                        }
                        CompactIconButton(
                            onClick = onOpenVariantsModal,
                            modifier = Modifier
                                .size(31.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Primary.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.People, contentDescription = "Variants", tint = ClayColors.Primary, modifier = Modifier.size(15.dp))
                        }
                        CompactIconButton(
                            onClick = onOpenVersionsModal,
                            modifier = Modifier
                                .size(31.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Sky.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Versions", tint = ClayColors.Sky, modifier = Modifier.size(15.dp))
                        }
                        ClayButton(
                            onClick = { triggerSave(true) },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Save", fontSize = 10.5.sp, maxLines = 1)
                        }
                    }
                }

                // 5-Tab Segmented Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    editorTabs.forEachIndexed { idx, tabTitle ->
                        ClayButton(
                            onClick = { activeTab = idx },
                            variant = if (activeTab == idx) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                            isDark = isDark
                        ) {
                            Text(tabTitle, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // 2. TABBED WORKSPACE BODY
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            when (activeTab) {
                // TAB 0: COVER IMAGE & BASIC IDENTITY
                0 -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 102.dp, height = 78.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(ClayColors.Primary.copy(alpha = 0.12f))
                                            .border(1.dp, ClayColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
                                            .clickable { coverPickerLauncher.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = thumbnailText.ifBlank { template.thumbnail },
                                            contentDescription = "Cover",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.65f))
                                                .padding(vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isUploadingCover) "⏳..." else "📷 Change",
                                                color = Color.White,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ClayButton(
                                            onClick = { coverPickerLauncher.launch("image/*") },
                                            variant = ClayButtonVariant.PINK,
                                            isDark = isDark,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (isUploadingCover) "Uploading..." else "Upload Cover Photo",
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                        ClayTextField(
                                            value = thumbnailText,
                                            onValueChange = { thumbnailText = it },
                                            label = "Cover Image URL",
                                            placeholder = "https://...",
                                            isDark = isDark
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = nameText,
                                            onValueChange = { nameText = it },
                                            label = "Template Name *",
                                            isDark = isDark
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = slugText,
                                            onValueChange = { slugText = it },
                                            label = "URL Slug",
                                            isDark = isDark
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    categoryOptions.forEach { cat ->
                                        val active = categoryText.equals(cat, ignoreCase = true)
                                        ClayButton(
                                            onClick = { categoryText = cat },
                                            variant = if (active) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                            isDark = isDark
                                        ) {
                                            Text(cat, fontSize = 9.5.sp, maxLines = 1)
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = categoryText,
                                            onValueChange = { categoryText = it },
                                            label = "Category",
                                            isDark = isDark
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = priorityText,
                                            onValueChange = { priorityText = it },
                                            label = "Priority (1-999)",
                                            isDark = isDark
                                        )
                                    }
                                }

                                ClayTextField(
                                    value = descriptionText,
                                    onValueChange = { descriptionText = it },
                                    label = "Short Description",
                                    isDark = isDark,
                                    singleLine = false
                                )
                            }
                        }
                    }
                }

                // TAB 1: PRICING, STATUS & 15 CAPABILITIES MATRIX
                1 -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Amber, ClayColors.Pink)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ClayButton(
                                        onClick = { tierMode = "free"; priceText = "0" },
                                        variant = if (tierMode == "free") ClayButtonVariant.EMERALD else ClayButtonVariant.SECONDARY,
                                        isDark = isDark
                                    ) { Text("Free (₹0)", fontSize = 10.sp, maxLines = 1) }

                                    ClayButton(
                                        onClick = { tierMode = "vip"; priceText = "49" },
                                        variant = if (priceText == "49") ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                        isDark = isDark
                                    ) { Text("VIP ₹49", fontSize = 10.sp, maxLines = 1) }

                                    ClayButton(
                                        onClick = { tierMode = "vip"; priceText = "59" },
                                        variant = if (priceText == "59") ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                                        isDark = isDark
                                    ) { Text("VIP ₹59", fontSize = 10.sp, maxLines = 1) }

                                    ClayButton(
                                        onClick = { tierMode = "vip"; priceText = "149" },
                                        variant = if (priceText == "149") ClayButtonVariant.AMBER else ClayButtonVariant.SECONDARY,
                                        isDark = isDark
                                    ) { Text("VIP ₹149", fontSize = 10.sp, maxLines = 1) }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = priceText,
                                            onValueChange = {
                                                priceText = it
                                                tierMode = if ((it.toDoubleOrNull() ?: 0.0) > 0.0) "vip" else "free"
                                            },
                                            label = "Custom Price (₹)",
                                            isDark = isDark
                                        )
                                    }
                                    ClayButton(
                                        onClick = { isPublished = !isPublished },
                                        variant = if (isPublished) ClayButtonVariant.EMERALD else ClayButtonVariant.AMBER,
                                        isDark = isDark
                                    ) {
                                        Text(if (isPublished) "✓ Live" else "Draft", fontSize = 10.5.sp, maxLines = 1)
                                    }
                                    ClayButton(
                                        onClick = { isFeatured = !isFeatured },
                                        variant = if (isFeatured) ClayButtonVariant.AMBER else ClayButtonVariant.SECONDARY,
                                        isDark = isDark
                                    ) {
                                        Text(if (isFeatured) "★ Featured" else "☆ Standard", fontSize = 10.5.sp, maxLines = 1)
                                    }
                                }

                                Divider(color = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Interactive Capabilities", fontSize = 12.sp, fontWeight = FontWeight.Black, color = textColor)
                                    ClayPill(text = "${selectedCapabilities.size}/15 Active", color = ClayColors.Primary, isDark = isDark)
                                }

                                // 3-Column Compact Capabilities Grid (Non-Overlapping)
                                allWebsiteCapabilities.chunked(3).forEach { rowCaps ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowCaps.forEach { (capKey, capLabel) ->
                                            val isChecked = selectedCapabilities.contains(capKey)
                                            ClayButton(
                                                onClick = {
                                                    selectedCapabilities = if (isChecked) {
                                                        selectedCapabilities - capKey
                                                    } else {
                                                        selectedCapabilities + capKey
                                                    }
                                                },
                                                variant = if (isChecked) ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                                                isDark = isDark,
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (isChecked) "✓ $capLabel" else capLabel,
                                                    fontSize = 9.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        repeat(3 - rowCaps.size) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: RELATIONSHIP VARIANTS (STRICTLY ONLY THIS TEMPLATE'S VARIANTS!)
                2 -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Emerald)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Template Variants (${templateSpecificVariants.size} Created)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = textColor
                                        )
                                        Text(
                                            text = "Showing only variants created specifically for ${template.name}.",
                                            fontSize = 10.sp,
                                            color = mutedColor
                                        )
                                    }
                                    if (templateSpecificVariants.isNotEmpty()) {
                                        ClayButton(
                                            onClick = onOpenVariantsModal,
                                            variant = ClayButtonVariant.PINK,
                                            isDark = isDark
                                        ) {
                                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Edit Copy & Theme", fontSize = 10.sp, maxLines = 1)
                                        }
                                    }
                                }

                                if (templateSpecificVariants.isEmpty()) {
                                    ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "Single-Edition Template (No multi-relationship variants declared)",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                            Text(
                                                text = "This template runs as a universal edition for all recipients. You can optionally add a custom variant below if needed.",
                                                fontSize = 10.sp,
                                                color = mutedColor
                                            )
                                        }
                                    }
                                } else {
                                    // 2-Column Grid of ONLY This Template's Variants
                                    templateSpecificVariants.chunked(2).forEach { group ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            group.forEach { vSlug ->
                                                val enabled = enabledVariants.contains(vSlug)
                                                val vObj = variantsList.firstOrNull { it.relationship == vSlug }
                                                val displayLabel = vObj?.relationshipLabel?.ifBlank { vSlug } ?: vSlug.replace("_", " ").replaceFirstChar { it.uppercase() }
                                                ClayButton(
                                                    onClick = {
                                                        val next = if (enabled && enabledVariants.size > 1) {
                                                            enabledVariants - vSlug
                                                        } else if (!enabled) {
                                                            enabledVariants + vSlug
                                                        } else enabledVariants
                                                        enabledVariants = next
                                                    },
                                                    variant = if (enabled) ClayButtonVariant.EMERALD else ClayButtonVariant.SECONDARY,
                                                    isDark = isDark,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = if (enabled) "✓ $displayLabel (Included)" else "☐ $displayLabel (Hidden)",
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            repeat(2 - group.size) {
                                                Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }

                                // Optional: Create a new variant specifically for this template
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = newVariantInput,
                                            onValueChange = { newVariantInput = it },
                                            label = "Add New Variant to This Template",
                                            placeholder = "e.g. sister, brother, best_friend",
                                            isDark = isDark
                                        )
                                    }
                                    ClayButton(
                                        onClick = {
                                            val clean = newVariantInput.trim().lowercase().replace(" ", "_").replace("-", "_")
                                            if (clean.isNotBlank()) {
                                                if (!enabledVariants.contains(clean)) {
                                                    enabledVariants = enabledVariants + clean
                                                }
                                                if (variantsList.none { it.relationship == clean }) {
                                                    val lbl = clean.replace("_", " ").replaceFirstChar { it.uppercase() }
                                                    variantsList = variantsList + TemplateVariantNode(
                                                        id = "var_$clean",
                                                        slug = clean.replace("_", "-"),
                                                        name = "$lbl Edition",
                                                        relationship = clean,
                                                        relationshipLabel = lbl,
                                                        enabled = true
                                                    )
                                                }
                                                newVariantInput = ""
                                            }
                                        },
                                        variant = ClayButtonVariant.PINK,
                                        isDark = isDark
                                    ) {
                                        Text("+ Add", fontSize = 10.5.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 3: SECTIONS & PHOTO SLOTS VISIBILITY
                3 -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Scope Switcher (Only shows "All" + this template's actual variants!)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Scope:", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = mutedColor)
                                    ClayButton(
                                        onClick = { selectedScope = "all" },
                                        variant = if (selectedScope == "all") ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                        isDark = isDark
                                    ) {
                                        Text("🌐 All", fontSize = 10.sp, maxLines = 1)
                                    }
                                    templateSpecificVariants.forEach { relSlug ->
                                        ClayButton(
                                            onClick = { selectedScope = relSlug },
                                            variant = if (selectedScope == relSlug) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                            isDark = isDark
                                        ) {
                                            Text(relSlug, fontSize = 10.sp, maxLines = 1)
                                        }
                                    }
                                }

                                sections.forEach { sec ->
                                    val topEnabled = isSectionEnabledForActiveScope(sec, true)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ClayColors.Primary.copy(alpha = 0.06f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(sec.label, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (sec.required) {
                                                    ClayPill(text = "REQ", color = ClayColors.Primary, isDark = isDark)
                                                }
                                            }
                                            ClaySwitch(
                                                checked = topEnabled,
                                                onCheckedChange = { checked ->
                                                    if (!sec.required) {
                                                        setSectionEnabledForActiveScope(sec.id, checked)
                                                    }
                                                },
                                                isDark = isDark
                                            )
                                        }

                                        if (sec.subsections.isNotEmpty()) {
                                            sec.subsections.forEach { sub ->
                                                val subEnabled = isSectionEnabledForActiveScope(sub, topEnabled)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("↳ ${sub.label}", fontSize = 10.5.sp, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        if (sub.photoSlotFieldId.isNotBlank()) {
                                                            ClayPill(text = "📷", color = ClayColors.Emerald, isDark = isDark)
                                                        }
                                                    }
                                                    ClaySwitch(
                                                        checked = subEnabled,
                                                        onCheckedChange = { checked ->
                                                            if (topEnabled && !sub.required) {
                                                                setSectionEnabledForActiveScope(sub.id, checked)
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

                // TAB 5: IN-APP INTERACTIVE LIVE PREVIEW
                5 -> {
                    item {
                        val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                        val liveUrl = "$base/template-preview.html?template=${slugText.ifBlank { template.slug }}"
                        if (showFullEditorPreview) {
                            FullscreenTemplatePreviewDialog(
                                title = nameText.ifBlank { template.name },
                                livePreviewUrl = liveUrl,
                                variantsList = variantsList,
                                actionsList = actions,
                                fieldsList = template.fields,
                                sectionsList = sections,
                                isDark = isDark,
                                onPublishNow = {
                                    showFullEditorPreview = false
                                    triggerSave(true)
                                },
                                onDismiss = { showFullEditorPreview = false }
                            )
                        }
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary, ClayColors.Pink)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            TemplateLivePreviewBox(
                                title = nameText.ifBlank { template.name },
                                livePreviewUrl = liveUrl,
                                variantsList = variantsList,
                                actionsList = actions,
                                fieldsList = template.fields,
                                sectionsList = sections,
                                isDark = isDark,
                                previewHeight = 420.dp,
                                showFieldTester = true,
                                onOpenFullscreen = { showFullEditorPreview = true }
                            )
                        }
                    }
                }

                // TAB 4: LOGICAL LINKS & FESTIVAL METADATA
                else -> {
                    item {
                        TopAccentCard(
                            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Amber)),
                            isDark = isDark,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Button Actions & Logical Links (${actions.size})", fontSize = 12.sp, fontWeight = FontWeight.Black, color = textColor)

                                actions.forEachIndexed { idx, act ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ClayColors.Pink.copy(alpha = 0.06f))
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${act.id} (${act.type})", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = ClayColors.Primary)
                                            ClaySwitch(
                                                checked = act.enabled,
                                                onCheckedChange = { en ->
                                                    if (!act.required) {
                                                        actions = actions.toMutableList().also { it[idx] = act.copy(enabled = en) }
                                                    }
                                                },
                                                isDark = isDark
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                ClayTextField(
                                                    value = act.label,
                                                    onValueChange = { newLbl ->
                                                        actions = actions.toMutableList().also { it[idx] = act.copy(label = newLbl) }
                                                    },
                                                    label = "Label",
                                                    isDark = isDark
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                ClayTextField(
                                                    value = act.target,
                                                    onValueChange = { newTarget ->
                                                        actions = actions.toMutableList().also { it[idx] = act.copy(target = newTarget) }
                                                    },
                                                    label = "Target URL / ID",
                                                    isDark = isDark
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            actionTypes.forEach { tType ->
                                                ClayButton(
                                                    onClick = {
                                                        val defaultLogical = when (tType) {
                                                            "template_link", "customizer" -> "template.customize"
                                                            "global_link" -> "global.explore"
                                                            else -> ""
                                                        }
                                                        actions = actions.toMutableList().also {
                                                            it[idx] = act.copy(type = tType, logicalLink = defaultLogical)
                                                        }
                                                    },
                                                    variant = if (act.type == tType) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                                    isDark = isDark
                                                ) {
                                                    Text(tType, fontSize = 9.5.sp, maxLines = 1)
                                                }
                                            }
                                        }

                                        if (act.type == "template_link" || act.type == "customizer" || act.type == "global_link") {
                                            val candidateLinks = if (act.type == "global_link") globalLogicalLinks else templateLogicalLinks
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                candidateLinks.forEach { lId ->
                                                    ClayButton(
                                                        onClick = {
                                                            actions = actions.toMutableList().also {
                                                                it[idx] = act.copy(logicalLink = lId)
                                                            }
                                                        },
                                                        variant = if (act.logicalLink == lId) ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                                                        isDark = isDark
                                                    ) {
                                                        Text(lId, fontSize = 9.5.sp, maxLines = 1)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Divider(color = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder)

                                Text("Festival Scheduler Metadata", fontSize = 12.sp, fontWeight = FontWeight.Black, color = textColor)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = festivalTagsCsv,
                                            onValueChange = { festivalTagsCsv = it },
                                            label = "Festival Tags",
                                            placeholder = "diwali, holi",
                                            isDark = isDark
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = festivalBadgeText,
                                            onValueChange = { festivalBadgeText = it },
                                            label = "Display Badge",
                                            placeholder = "✨ Diwali",
                                            isDark = isDark
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = suggestedDaysBefore,
                                            onValueChange = { suggestedDaysBefore = it },
                                            label = "Days Before (F-)",
                                            isDark = isDark
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        ClayTextField(
                                            value = suggestedDaysAfter,
                                            onValueChange = { suggestedDaysAfter = it },
                                            label = "Days After (F+)",
                                            isDark = isDark
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    campaignCompatOptions.forEach { cMode ->
                                        ClayButton(
                                            onClick = { campaignCompatMode = cMode },
                                            variant = if (campaignCompatMode == cMode) ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                                            isDark = isDark
                                        ) {
                                            Text(cMode, fontSize = 9.5.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. COMPACT BOTTOM ACTION BAR
        TopAccentCard(
            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
            isDark = isDark,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactIconButton(
                    onClick = {
                        sections = sections.map { top ->
                            top.copy(
                                enabled = true,
                                subsections = top.subsections.map { it.copy(enabled = true) }
                            )
                        }
                        onResetDefaults(selectedScope)
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(ClayColors.Rose.copy(alpha = 0.14f))
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset Defaults", tint = ClayColors.Rose, modifier = Modifier.size(16.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClayButton(
                        onClick = { triggerSave(false) },
                        variant = ClayButtonVariant.SECONDARY,
                        isDark = isDark
                    ) {
                        Text("Save Draft", fontSize = 10.5.sp, maxLines = 1)
                    }
                    ClayButton(
                        onClick = { triggerSave(true) },
                        variant = ClayButtonVariant.PINK,
                        isDark = isDark
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save & Publish", fontSize = 10.5.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun RelationshipVariantsManagerDialog(
    template: TemplateItem,
    isDark: Boolean,
    serverUrl: String,
    onDismiss: () -> Unit,
    onSaveVariant: (TemplateVariantNode) -> Unit,
    onCloneVariant: (sourceRel: String, targetRel: String, newName: String) -> Unit
) {
    val context = LocalContext.current
    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    // Strictly only variants created for this template!
    val allRels = remember(template.id, template.variantsList, template.enabledVariants) {
        (template.variantsList.map { it.relationship } + template.enabledVariants)
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    var selectedRel by remember(template.id, allRels) {
        mutableStateOf(allRels.firstOrNull().orEmpty())
    }
    val currentVar = template.variantsList.firstOrNull { it.relationship == selectedRel }
        ?: TemplateVariantNode(
            id = "var_${selectedRel.ifBlank { "default" }}",
            slug = selectedRel.ifBlank { "default" }.replace("_", "-"),
            name = "${template.name} — ${selectedRel.ifBlank { "Default" }.replace("_", " ").replaceFirstChar { it.uppercase() }} Edition",
            relationship = selectedRel.ifBlank { "default" },
            relationshipLabel = selectedRel.ifBlank { "Default" }.replace("_", " ").replaceFirstChar { it.uppercase() }
        )

    var varName by remember(currentVar) { mutableStateOf(currentVar.name) }
    var varDesc by remember(currentVar) { mutableStateOf(currentVar.description) }
    var varEnabled by remember(currentVar) { mutableStateOf(currentVar.enabled) }
    var primaryHex by remember(currentVar) { mutableStateOf(currentVar.themePrimary) }
    var secondaryHex by remember(currentVar) { mutableStateOf(currentVar.themeSecondary) }
    var accentHex by remember(currentVar) { mutableStateOf(currentVar.themeAccent) }
    var heroTitle by remember(currentVar) { mutableStateOf(currentVar.messages["hero_title"] ?: "") }
    var heroSubtitle by remember(currentVar) { mutableStateOf(currentVar.messages["hero_subtitle"] ?: "") }
    var customMessage by remember(currentVar) { mutableStateOf(currentVar.messages["custom_message"] ?: "") }
    var senderName by remember(currentVar) { mutableStateOf(currentVar.messages["sender_name"] ?: "") }

    var cloneTargetRel by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        TopAccentCard(
            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)),
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
                            text = "Template Variants (${allRels.size})",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "${template.name} • Only variants created for this template",
                            fontSize = 10.sp,
                            color = mutedColor
                        )
                    }
                    CompactIconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedColor)
                    }
                }

                if (allRels.isEmpty()) {
                    ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "No relationship variants created for this template yet.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "This template runs as a single universal edition. Enter a relationship below to create its first variant:",
                                fontSize = 10.5.sp,
                                color = mutedColor
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ClayTextField(
                                        value = cloneTargetRel,
                                        onValueChange = { cloneTargetRel = it },
                                        placeholder = "e.g. sister, brother, best_friend",
                                        isDark = isDark
                                    )
                                }
                                ClayButton(
                                    onClick = {
                                        val clean = cloneTargetRel.trim().lowercase().replace(" ", "_")
                                        if (clean.isNotBlank()) {
                                            onCloneVariant("family", clean, "${template.name} — $clean")
                                            selectedRel = clean
                                            cloneTargetRel = ""
                                        }
                                    },
                                    variant = ClayButtonVariant.PINK,
                                    isDark = isDark
                                ) {
                                    Text("+ Create Variant", fontSize = 10.5.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                } else {
                    // Variant Selector Tabs (Only this template's variants!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allRels.forEach { r ->
                            ClayButton(
                                onClick = { selectedRel = r },
                                variant = if (selectedRel == r) ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                                isDark = isDark
                            ) {
                                Text(r.replace("_", " ").uppercase(), fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }

                    ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Edition: ${currentVar.relationshipLabel} (${currentVar.slug})",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor
                                )
                                ClaySwitch(
                                    checked = varEnabled,
                                    onCheckedChange = { varEnabled = it },
                                    isDark = isDark
                                )
                            }

                            ClayTextField(value = varName, onValueChange = { varName = it }, label = "Variant Display Title", isDark = isDark)
                            ClayTextField(value = varDesc, onValueChange = { varDesc = it }, label = "Variant Description", isDark = isDark)

                            Text("CSS :root THEME TOKENS (--wm-*)", fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = mutedColor)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ClayTextField(value = primaryHex, onValueChange = { primaryHex = it }, label = "--wm-primary", isDark = isDark)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ClayTextField(value = secondaryHex, onValueChange = { secondaryHex = it }, label = "--wm-secondary", isDark = isDark)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ClayTextField(value = accentHex, onValueChange = { accentHex = it }, label = "--wm-accent", isDark = isDark)
                                }
                            }

                            Text("RELATIONSHIP-TAILORED DEFAULT MESSAGES", fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = mutedColor)
                            ClayTextField(value = heroTitle, onValueChange = { heroTitle = it }, label = "hero_title", isDark = isDark)
                            ClayTextField(value = heroSubtitle, onValueChange = { heroSubtitle = it }, label = "hero_subtitle", isDark = isDark)
                            ClayTextField(value = customMessage, onValueChange = { customMessage = it }, label = "custom_message", isDark = isDark)
                            ClayTextField(value = senderName, onValueChange = { senderName = it }, label = "sender_name", isDark = isDark)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ClayButton(
                                    onClick = {
                                        val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                                        val url = "$base/template-preview.html?template=${template.slug}&rel=$selectedRel"
                                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                    },
                                    variant = ClayButtonVariant.SECONDARY,
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Preview", fontSize = 10.5.sp, maxLines = 1)
                                }
                                ClayButton(
                                    onClick = {
                                        onSaveVariant(
                                            currentVar.copy(
                                                name = varName,
                                                description = varDesc,
                                                enabled = varEnabled,
                                                status = if (varEnabled) "published" else "disabled",
                                                themePrimary = primaryHex,
                                                themeSecondary = secondaryHex,
                                                themeAccent = accentHex,
                                                messages = mapOf(
                                                    "hero_title" to heroTitle,
                                                    "hero_subtitle" to heroSubtitle,
                                                    "custom_message" to customMessage,
                                                    "sender_name" to senderName
                                                )
                                            )
                                        )
                                    },
                                    variant = ClayButtonVariant.PINK,
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Save Variant", fontSize = 10.5.sp, maxLines = 1)
                                }
                            }
                        }
                    }

                    // Clone Variant Box
                    ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Clone '$selectedRel' to New Relationship", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ClayTextField(
                                        value = cloneTargetRel,
                                        onValueChange = { cloneTargetRel = it },
                                        placeholder = "e.g. mother, father, husband",
                                        isDark = isDark
                                    )
                                }
                                ClayButton(
                                    onClick = {
                                        if (cloneTargetRel.isNotBlank()) {
                                            onCloneVariant(selectedRel, cloneTargetRel, "")
                                            selectedRel = cloneTargetRel.lowercase().trim().replace(" ", "_")
                                            cloneTargetRel = ""
                                        }
                                    },
                                    variant = ClayButtonVariant.EMERALD,
                                    isDark = isDark
                                ) {
                                    Text("Clone", fontSize = 10.5.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateVersionControlDialog(
    template: TemplateItem,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onUploadNewVersion: () -> Unit,
    onRollback: (versionId: String) -> Unit,
    onPublishVersion: (versionId: String) -> Unit,
    onValidateVersion: (versionId: String) -> Unit,
    onPreview: () -> Unit
) {
    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    val versions = remember(template) {
        if (template.versionsList.isNotEmpty()) {
            template.versionsList
        } else {
            listOf(
                TemplateVersionItem(
                    id = template.currentVersionId.ifBlank { "tv_${template.id}_v1" },
                    templateId = template.id,
                    versionNumber = 1,
                    versionLabel = template.templateVersion,
                    status = template.status,
                    isCurrent = true,
                    filesCount = template.filesCount,
                    storageProvider = template.storageProvider
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        TopAccentCard(
            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)),
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
                            text = "${template.name} — Versions",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "ID: ${template.id} • Live: v${template.templateVersion}",
                            fontSize = 10.5.sp,
                            color = mutedColor
                        )
                    }
                    CompactIconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedColor)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${versions.size} version(s)", fontSize = 11.sp, color = mutedColor)
                    ClayButton(
                        onClick = onUploadNewVersion,
                        variant = ClayButtonVariant.PINK,
                        isDark = isDark
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Version", fontSize = 10.5.sp, maxLines = 1)
                    }
                }

                versions.forEach { ver ->
                    ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "v${ver.versionLabel}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                                StatusBadgePill(
                                    text = if (ver.isCurrent) "CURRENT LIVE" else ver.status.uppercase(),
                                    isSuccess = ver.isCurrent || ver.status.equals("published", true),
                                    isDark = isDark
                                )
                                ClayPill(text = "${ver.filesCount} files", color = ClayColors.Primary, isDark = isDark)
                            }
                            Text(
                                text = "ID: ${ver.id} • UTP v${template.schemaVersion}",
                                fontSize = 10.sp,
                                color = mutedColor
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ClayButton(
                                    onClick = onPreview,
                                    variant = ClayButtonVariant.SECONDARY,
                                    isDark = isDark
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Preview", fontSize = 10.sp, maxLines = 1)
                                }
                                ClayButton(
                                    onClick = { onValidateVersion(ver.id) },
                                    variant = ClayButtonVariant.EMERALD,
                                    isDark = isDark
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Verify UTP", fontSize = 10.sp, maxLines = 1)
                                }
                                if (!ver.isCurrent) {
                                    ClayButton(
                                        onClick = { onRollback(ver.id) },
                                        variant = ClayButtonVariant.AMBER,
                                        isDark = isDark
                                    ) {
                                        Text("⏪ Rollback", fontSize = 10.sp, maxLines = 1)
                                    }
                                    ClayButton(
                                        onClick = { onPublishVersion(ver.id) },
                                        variant = ClayButtonVariant.PINK,
                                        isDark = isDark
                                    ) {
                                        Text("Publish", fontSize = 10.sp, maxLines = 1)
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
