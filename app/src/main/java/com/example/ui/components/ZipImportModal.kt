package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.clay.*
import org.json.JSONObject

/**
 * Interactive Mobile Live Preview Component for both Staged Template ZIPs (before upload)
 * and Live Published Templates.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TemplateLivePreviewBox(
    title: String,
    stagedHtml: String = "",
    baseUrl: String = "https://wishmagic.pages.dev/",
    livePreviewUrl: String = "",
    variantsList: List<TemplateVariantNode> = emptyList(),
    actionsList: List<TemplateActionNode> = emptyList(),
    fieldsList: List<TemplateFieldNode> = emptyList(),
    sectionsList: List<TemplateSectionNode> = emptyList(),
    isDark: Boolean,
    previewHeight: Dp = 360.dp,
    showFieldTester: Boolean = true,
    onOpenFullscreen: (() -> Unit)? = null
) {
    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isPageLoaded by remember { mutableStateOf(false) }
    var selectedVariantSlug by remember(variantsList) {
        mutableStateOf(
            variantsList.firstOrNull { it.isDefault }?.slug
                ?: variantsList.firstOrNull()?.slug
                ?: ""
        )
    }
    var reloadNonce by remember { mutableStateOf(0) }
    var showInlineFieldEditor by remember { mutableStateOf(false) }

    val liveFieldValues = remember(fieldsList) {
        val map = mutableStateMapOf<String, String>()
        fieldsList.forEach { f ->
            if (f.defaultValue.isNotBlank()) {
                map[f.id] = f.defaultValue
            }
        }
        if ("recipient_name" !in map) map["recipient_name"] = "Sweety ✨"
        if ("sender_name" !in map) map["sender_name"] = "With Love ❤️"
        map
    }

    // Push section visibility changes to WebView whenever sectionsList changes
    LaunchedEffect(sectionsList, isPageLoaded) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (!isPageLoaded || sectionsList.isEmpty()) return@LaunchedEffect
        val visJson = JSONObject()
        fun collectVis(nodes: List<TemplateSectionNode>) {
            for (s in nodes) {
                visJson.put(s.id, if (s.required) true else s.enabled)
                if (s.subsections.isNotEmpty()) collectVis(s.subsections)
            }
        }
        collectVis(sectionsList)
        val payload = JSONObject().apply { put("visibility", visJson) }.toString()
        wv.evaluateJavascript("if(window.__wmApplyLiveState){window.__wmApplyLiveState($payload);}", null)
    }

    fun selectVariantInPreview(v: TemplateVariantNode) {
        selectedVariantSlug = v.slug
        val wv = webViewRef ?: return
        val safeSlug = v.slug.replace("'", "\\'")
        wv.evaluateJavascript("if(window.__wmSelectVariant){window.__wmSelectVariant('$safeSlug');}", null)
    }

    fun triggerActionInPreview(act: TemplateActionNode) {
        val wv = webViewRef ?: return
        val safeId = act.id.replace("'", "\\'")
        wv.evaluateJavascript("if(window.__wmTriggerAction){window.__wmTriggerAction('$safeId');}", null)
    }

    fun updateFieldInPreview(fieldId: String, value: String) {
        liveFieldValues[fieldId] = value
        val wv = webViewRef ?: return
        val fieldsJson = JSONObject().apply { put(fieldId, value) }
        val payload = JSONObject().apply { put("fields", fieldsJson) }.toString()
        wv.evaluateJavascript("if(window.__wmApplyLiveState){window.__wmApplyLiveState($payload);}", null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Top Preview Control Bar
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
                StatusBadgePill(
                    text = if (stagedHtml.isNotBlank()) "🟢 ZIP LIVE PREVIEW" else "🟢 LIVE PREVIEW",
                    isSuccess = true,
                    isDark = isDark
                )
                if (selectedVariantSlug.isNotBlank()) {
                    ClayPill(
                        text = selectedVariantSlug.replace("_", " ").uppercase(),
                        color = ClayColors.Pink,
                        isDark = isDark
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (showFieldTester) {
                    CompactIconButton(
                        onClick = { showInlineFieldEditor = !showInlineFieldEditor },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (showInlineFieldEditor) ClayColors.Pink.copy(alpha = 0.22f)
                                else ClayColors.Primary.copy(alpha = 0.14f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Test Fields",
                            tint = if (showInlineFieldEditor) ClayColors.Pink else ClayColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                CompactIconButton(
                    onClick = { reloadNonce++ },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClayColors.Sky.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Preview",
                        tint = ClayColors.Sky,
                        modifier = Modifier.size(15.dp)
                    )
                }
                if (onOpenFullscreen != null) {
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink)))
                            .clickable { onOpenFullscreen() }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen Preview",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Full Preview",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Interactive WebView Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D091A))
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(ClayColors.Primary, ClayColors.Pink, ClayColors.Emerald)),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            key(reloadNonce, stagedHtml.hashCode(), livePreviewUrl) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.parseColor("#0D091A"))
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                builtInZoomControls = false
                                displayZoomControls = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }
                            isVerticalScrollBarEnabled = true
                            isHorizontalScrollBarEnabled = false
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isPageLoaded = true
                                    if (selectedVariantSlug.isNotBlank()) {
                                        val safeSlug = selectedVariantSlug.replace("'", "\\'")
                                        view?.evaluateJavascript(
                                            "if(window.__wmSelectVariant){window.__wmSelectVariant('$safeSlug');}",
                                            null
                                        )
                                    }
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val reqUrl = request?.url?.toString().orEmpty()
                                    if (reqUrl.startsWith("file://") || reqUrl.startsWith("data:") || reqUrl.startsWith("about:")) {
                                        return false
                                    }
                                    if (stagedHtml.isNotBlank()) {
                                        // Keep staged preview inside the sandbox
                                        return true
                                    }
                                    return false
                                }
                            }

                            if (stagedHtml.isNotBlank()) {
                                loadDataWithBaseURL(
                                    baseUrl.ifBlank { "https://wishmagic.pages.dev/" },
                                    stagedHtml,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            } else if (livePreviewUrl.isNotBlank()) {
                                loadUrl(livePreviewUrl)
                            }
                            webViewRef = this
                        }
                    },
                    update = { wv ->
                        webViewRef = wv
                    }
                )
            }
        }

        // Live Variant / Theme Switcher Strip
        if (variantsList.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🎨 TAP VARIANT / THEME TO PREVIEW LIVE (${variantsList.size}):",
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
                    variantsList.forEach { v ->
                        val isSelected = selectedVariantSlug == v.slug || selectedVariantSlug == v.relationship
                        val dotColor = runCatching {
                            Color(android.graphics.Color.parseColor(v.themePrimary))
                        }.getOrDefault(ClayColors.Amber)

                        Row(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary))
                                    else Brush.horizontalGradient(
                                        listOf(
                                            ClayColors.Primary.copy(alpha = 0.12f),
                                            ClayColors.Pink.copy(alpha = 0.12f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.6f) else ClayColors.Primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(9.dp)
                                )
                                .clickable { selectVariantInPreview(v) }
                                .padding(horizontal = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                            Text(
                                text = v.relationshipLabel.ifBlank { v.name },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else textColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Interactive Action Tester Strip
        if (actionsList.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "⚡ TEST TEMPLATE ACTIONS IN PREVIEW (${actionsList.size}):",
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
                    actionsList.forEach { act ->
                        ClayButton(
                            onClick = { triggerActionInPreview(act) },
                            variant = if (act.type == "interactive" || act.handler.isNotBlank()) ClayButtonVariant.AMBER else ClayButtonVariant.SECONDARY,
                            isDark = isDark,
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (act.type == "interactive" || act.handler.isNotBlank()) Icons.Default.AutoAwesome else Icons.Default.TouchApp,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(act.label, fontSize = 9.5.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Expandable Live Field Tester
        if (showFieldTester && showInlineFieldEditor) {
            ClayWellBox(isDark = isDark, contentPadding = PaddingValues(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✍️ LIVE CUSTOMIZER FIELD TESTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayColors.Pink
                        )
                        Text(
                            text = "Updates Preview Instantly",
                            fontSize = 9.sp,
                            color = mutedColor
                        )
                    }
                    val textFields = fieldsList.filter { it.type == "text" || it.type == "textarea" }.take(4).ifEmpty {
                        listOf(
                            TemplateFieldNode(id = "recipient_name", label = "Recipient Name", defaultValue = "Sweety ✨"),
                            TemplateFieldNode(id = "sender_name", label = "Sender Name", defaultValue = "With Love ❤️"),
                            TemplateFieldNode(id = "custom_message", label = "Message", defaultValue = "Wishing you the happiest celebration!")
                        )
                    }
                    textFields.forEach { tf ->
                        val curVal = liveFieldValues[tf.id] ?: tf.defaultValue
                        ClayTextField(
                            value = curVal,
                            onValueChange = { updateFieldInPreview(tf.id, it) },
                            label = "${tf.label} (${tf.id})",
                            placeholder = tf.defaultValue.ifBlank { "Type to test live..." },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenTemplatePreviewDialog(
    title: String,
    stagedHtml: String = "",
    baseUrl: String = "https://wishmagic.pages.dev/",
    livePreviewUrl: String = "",
    variantsList: List<TemplateVariantNode> = emptyList(),
    actionsList: List<TemplateActionNode> = emptyList(),
    fieldsList: List<TemplateFieldNode> = emptyList(),
    sectionsList: List<TemplateSectionNode> = emptyList(),
    isDark: Boolean,
    onPublishNow: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (!isDark) ClayColors.LightBg else ClayColors.DarkBg)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        ClayButton(
                            onClick = onDismiss,
                            variant = ClayButtonVariant.SECONDARY,
                            isDark = isDark,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Back", fontSize = 10.5.sp)
                        }
                        Text(
                            text = title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (onPublishNow != null) {
                        ClayButton(
                            onClick = onPublishNow,
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Publish", fontSize = 10.5.sp)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    TemplateLivePreviewBox(
                        title = title,
                        stagedHtml = stagedHtml,
                        baseUrl = baseUrl,
                        livePreviewUrl = livePreviewUrl,
                        variantsList = variantsList,
                        actionsList = actionsList,
                        fieldsList = fieldsList,
                        sectionsList = sectionsList,
                        isDark = isDark,
                        previewHeight = 560.dp,
                        showFieldTester = true,
                        onOpenFullscreen = null
                    )
                }
            }
        }
    }
}

@Composable
fun ZipImportModal(
    pendingZip: PendingZipPackage,
    isDarkTheme: Boolean,
    onPublishLive: (updateMode: String, templateName: String, price: Double) -> Unit,
    onOpenInStudio: () -> Unit,
    onDismiss: () -> Unit
) {
    var templateName by remember(pendingZip) { mutableStateOf(pendingZip.suggestedName) }
    var templatePrice by remember(pendingZip) { mutableStateOf(pendingZip.detectedPrice.toInt().toString()) }
    var updateMode by remember { mutableStateOf("full") }
    var showFullscreenPreview by remember { mutableStateOf(false) }

    val textColor = if (!isDarkTheme) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDarkTheme) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    if (showFullscreenPreview) {
        FullscreenTemplatePreviewDialog(
            title = templateName.ifBlank { pendingZip.suggestedName },
            stagedHtml = pendingZip.stagedPreviewHtml,
            baseUrl = pendingZip.stagedPreviewBaseUrl,
            variantsList = pendingZip.variantsList,
            actionsList = pendingZip.actions,
            fieldsList = pendingZip.fields,
            sectionsList = pendingZip.sections,
            isDark = isDarkTheme,
            onPublishNow = {
                showFullscreenPreview = false
                val priceVal = templatePrice.toDoubleOrNull() ?: 0.0
                onPublishLive(updateMode, templateName, priceVal)
            },
            onDismiss = { showFullscreenPreview = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.68f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Emerald, ClayColors.Primary, ClayColors.Pink)),
                isDark = isDarkTheme,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 540.dp)
                    .heightIn(max = 720.dp)
                    .clickable(enabled = false, onClick = {})
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (pendingZip.extractedCoverUri.isNotBlank()) {
                                AsyncImage(
                                    model = pendingZip.extractedCoverUri,
                                    contentDescription = pendingZip.suggestedName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ClayColors.Primary.copy(alpha = 0.15f))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ClayColors.Emerald.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderZip,
                                        contentDescription = null,
                                        tint = ClayColors.Emerald,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pendingZip.suggestedName,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${pendingZip.fileName} • ${pendingZip.sizeKb} KB • UTP v${pendingZip.detectedSchemaVersion}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayColors.Emerald,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        CompactIconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedColor, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Cloud Routing & Manifest Summary Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        ClayPill(
                            text = "${pendingZip.variantsList.size.coerceAtLeast(pendingZip.detectedVariants.size)} Variants",
                            color = ClayColors.Primary,
                            isDark = isDarkTheme
                        )
                        ClayPill(
                            text = "${pendingZip.detectedSectionsCount} Sections",
                            color = ClayColors.Pink,
                            isDark = isDarkTheme
                        )
                        ClayPill(
                            text = "${pendingZip.detectedFieldsCount} Fields",
                            color = ClayColors.Emerald,
                            isDark = isDarkTheme
                        )
                        ClayPill(
                            text = "ImgBB: ${pendingZip.imagesCount} Imgs",
                            color = ClayColors.Amber,
                            isDark = isDarkTheme
                        )
                        ClayPill(
                            text = "B2: ${pendingZip.codeFilesCount + pendingZip.audioCount} Files",
                            color = ClayColors.Sky,
                            isDark = isDarkTheme
                        )
                    }

                    // Live Interactive Preview Inside Modal!
                    if (pendingZip.stagedPreviewHtml.isNotBlank()) {
                        TemplateLivePreviewBox(
                            title = pendingZip.suggestedName,
                            stagedHtml = pendingZip.stagedPreviewHtml,
                            baseUrl = pendingZip.stagedPreviewBaseUrl,
                            variantsList = pendingZip.variantsList,
                            actionsList = pendingZip.actions,
                            fieldsList = pendingZip.fields,
                            sectionsList = pendingZip.sections,
                            isDark = isDarkTheme,
                            previewHeight = 250.dp,
                            showFieldTester = true,
                            onOpenFullscreen = { showFullscreenPreview = true }
                        )
                    }

                    // Quick Name & Price Row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = templateName,
                                onValueChange = { templateName = it },
                                label = "Template Name",
                                placeholder = "e.g. Happy Birthday Sweety",
                                isDark = isDarkTheme
                            )
                        }
                        Box(modifier = Modifier.width(96.dp)) {
                            ClayTextField(
                                value = templatePrice,
                                onValueChange = { templatePrice = it },
                                label = "Price (₹)",
                                placeholder = "0",
                                isDark = isDarkTheme
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClayButton(
                            onClick = { updateMode = "full" },
                            variant = if (updateMode == "full") ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f),
                            isDark = isDarkTheme
                        ) {
                            Text("Mode 1: Full ZIP", fontSize = 10.sp)
                        }
                        ClayButton(
                            onClick = { updateMode = "patch" },
                            variant = if (updateMode == "patch") ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f),
                            isDark = isDarkTheme
                        ) {
                            Text("Mode 2: Diff Patch", fontSize = 10.sp)
                        }
                    }

                    GradientPillButton(
                        text = "🚀 1-Tap Publish to Cloudflare B2, ImgBB & D1",
                        onClick = {
                            val priceVal = templatePrice.toDoubleOrNull() ?: 0.0
                            onPublishLive(updateMode, templateName, priceVal)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ClayButton(
                        onClick = onOpenInStudio,
                        variant = ClayButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                        isDark = isDarkTheme
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Open Full 5-Tab Upload Studio (Cover, Variants, Sections)", fontSize = 10.5.sp)
                    }
                }
            }
        }
    }
}
