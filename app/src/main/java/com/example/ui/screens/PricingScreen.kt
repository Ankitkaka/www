package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VipPlanItem
import com.example.ui.AdminViewModel
import com.example.ui.clay.*

@Composable
fun PricingScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val pricing by viewModel.pricingConfig.collectAsState()
    val coupons by viewModel.coupons.collectAsState()

    var slugPrice by remember(pricing.customSlugPrice) { mutableStateOf(pricing.customSlugPrice.toString()) }
    var hdPrice by remember(pricing.hdMediaPrice) { mutableStateOf(pricing.hdMediaPrice.toString()) }
    var comboPrice by remember(pricing.freeTemplateComboPrice) { mutableStateOf(pricing.freeTemplateComboPrice.toString()) }
    var addonPrice by remember(pricing.premiumComboAddonPrice) { mutableStateOf(pricing.premiumComboAddonPrice.toString()) }
    var vipPrice by remember(pricing.vipPassMonthlyPrice) { mutableStateOf(pricing.vipPassMonthlyPrice.toString()) }
    var refRequired by remember(pricing.referralRequiredCount) { mutableStateOf(pricing.referralRequiredCount.toString()) }

    var showCreateCouponModal by remember { mutableStateOf(false) }
    var editingVipPlan by remember { mutableStateOf<VipPlanItem?>(null) }
    var isCreatingNewVipPlan by remember { mutableStateOf(false) }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    val clVal = slugPrice.toIntOrNull() ?: 59
    val hdVal = hdPrice.toIntOrNull() ?: 59
    val fcVal = comboPrice.toIntOrNull() ?: 99
    val paVal = addonPrice.toIntOrNull() ?: 70
    val vpVal = vipPrice.toIntOrNull() ?: 49
    val rfVal = refRequired.toIntOrNull() ?: 7

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Compact Hero Banner
        item {
            CosmicHeroBanner(
                welcomePillText = "👑 VIP Plans, Combos & Coupons Studio",
                titlePrefix = "VIP Plans &",
                pinkAccentWord = "Pricing",
                titleSuffix = "(${pricing.vipPlans.size})",
                subtitle = "Create, edit (including ₹$vpVal VIP Pass), or delete VIP plans, set free perks, and sync to the Main Website.",
                primaryButtonText = "+ New VIP Plan",
                primaryButtonIcon = Icons.Default.AddCircle,
                onPrimaryClick = {
                    editingVipPlan = null
                    isCreatingNewVipPlan = true
                },
                secondaryButtonText = "+ Coupon",
                secondaryButtonIcon = Icons.Default.LocalOffer,
                onSecondaryClick = { showCreateCouponModal = true }
            )
        }

        // 2. VIP PLANS & FREE PERKS STUDIO (Create, Edit ₹49 & Default Plans, Delete, Set Free Perks)
        item {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary, ClayColors.Amber)),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ClayPill(text = "👑 VIP PLANS STUDIO", color = ClayColors.Pink, isDark = isDark)
                                ClayPill(text = "${pricing.vipPlans.size} Active", color = ClayColors.Primary, isDark = isDark)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Manage All VIP Plans & Free Perks",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CompactIconButton(
                                onClick = { viewModel.restoreDefaultVipPlans() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(ClayColors.Primary.copy(alpha = 0.14f))
                                    .border(1.dp, ClayColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Restore Default Plans",
                                    tint = ClayColors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            CompactIconButton(
                                onClick = {
                                    editingVipPlan = null
                                    isCreatingNewVipPlan = true
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)))
                                    .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(9.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create New VIP Plan",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (pricing.vipPlans.isEmpty()) {
                        Text(
                            text = "No VIP plans configured. Tap + to create a plan or Restore icon for defaults.",
                            color = mutedColor,
                            fontSize = 11.5.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            pricing.vipPlans.forEach { plan ->
                                CompactVipPlanRowCard(
                                    plan = plan,
                                    isDark = isDark,
                                    textColor = textColor,
                                    mutedColor = mutedColor,
                                    onEdit = {
                                        editingVipPlan = plan
                                        isCreatingNewVipPlan = false
                                    },
                                    onDelete = {
                                        viewModel.deleteVipPlan(plan.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Compact 2-Column Quick Price & Referral Editor
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
                        Text(
                            text = "Quick Price & Referral Controls",
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        ClayPill(text = "Live D1 Sync", color = ClayColors.Emerald, isDark = isDark)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = vipPrice,
                                onValueChange = { vipPrice = it },
                                label = "👑 VIP Pass (₹)",
                                isDark = isDark
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = slugPrice,
                                onValueChange = { slugPrice = it },
                                label = "🔗 Custom Link (₹)",
                                isDark = isDark
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = hdPrice,
                                onValueChange = { hdPrice = it },
                                label = "🖼️ HD + Style (₹)",
                                isDark = isDark
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = comboPrice,
                                onValueChange = { comboPrice = it },
                                label = "🔥 Free Combo (₹)",
                                isDark = isDark
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = addonPrice,
                                onValueChange = { addonPrice = it },
                                label = "👑 Prem Add-On (+₹)",
                                isDark = isDark
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = refRequired,
                                onValueChange = { refRequired = it },
                                label = "🎁 Referral Friends",
                                isDark = isDark
                            )
                        }
                    }

                    ClayButton(
                        onClick = {
                            viewModel.savePricing(clVal, hdVal, fcVal, paVal, vpVal, rfVal)
                        },
                        variant = ClayButtonVariant.PINK,
                        isDark = isDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Save Prices (VIP ₹$vpVal • Link ₹$clVal • Combo ₹$fcVal)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 4. Compact Promo Coupons Ledger
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Promo Coupons",
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            ClayPill(text = "${coupons.size}", color = ClayColors.Emerald, isDark = isDark)
                        }
                        CompactIconButton(
                            onClick = { showCreateCouponModal = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(ClayColors.Emerald.copy(alpha = 0.16f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Coupon",
                                tint = ClayColors.Emerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (coupons.isEmpty()) {
                        Text(
                            text = "No active coupons.",
                            color = mutedColor,
                            fontSize = 11.5.sp
                        )
                    } else {
                        coupons.forEach { coupon ->
                            ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = coupon.code,
                                                color = ClayColors.Amber,
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Black
                                            )
                                            ClayPill(text = coupon.typeLabel, color = ClayColors.Pink, isDark = isDark)
                                        }
                                        Text(
                                            text = "${coupon.redemptions} used • Exp: ${coupon.expiry}",
                                            color = mutedColor,
                                            fontSize = 10.sp
                                        )
                                    }
                                    CompactIconButton(
                                        onClick = { viewModel.deleteCoupon(coupon.code) },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ClayColors.Rose.copy(alpha = 0.14f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Coupon",
                                            tint = ClayColors.Rose,
                                            modifier = Modifier.size(16.dp)
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

    // Create / Edit VIP Plan Dialog
    if (isCreatingNewVipPlan || editingVipPlan != null) {
        VipPlanEditorDialog(
            initialPlan = editingVipPlan,
            isDark = isDark,
            onDismiss = {
                isCreatingNewVipPlan = false
                editingVipPlan = null
            },
            onSave = { savedPlan ->
                viewModel.saveVipPlan(savedPlan)
                isCreatingNewVipPlan = false
                editingVipPlan = null
            }
        )
    }

    // Create New Coupon Dialog
    if (showCreateCouponModal) {
        var newCode by remember { mutableStateOf("") }
        var discountType by remember { mutableStateOf("percentage") }
        var discountVal by remember { mutableStateOf("50") }
        var expiryText by remember { mutableStateOf("Dec 31, 2026") }

        Dialog(onDismissRequest = { showCreateCouponModal = false }) {
            TopAccentCard(
                accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary)),
                isDark = isDark,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Create Promo Coupon",
                        color = textColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    ClayTextField(
                        value = newCode,
                        onValueChange = { newCode = it.uppercase() },
                        label = "Coupon Code (e.g. DIWALI50)",
                        isDark = isDark
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ClayButton(
                            onClick = { discountType = "percentage" },
                            variant = if (discountType == "percentage") ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("% OFF", fontSize = 11.sp)
                        }
                        ClayButton(
                            onClick = { discountType = "flat" },
                            variant = if (discountType == "flat") ClayButtonVariant.PINK else ClayButtonVariant.SECONDARY,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Flat ₹ OFF", fontSize = 11.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = discountVal,
                                onValueChange = { discountVal = it },
                                label = if (discountType == "percentage") "Discount %" else "Flat ₹",
                                isDark = isDark
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = expiryText,
                                onValueChange = { expiryText = it },
                                label = "Expiry Date",
                                isDark = isDark
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateCouponModal = false }) {
                            Text("Cancel", color = mutedColor)
                        }
                        Spacer(Modifier.width(6.dp))
                        ClayButton(
                            onClick = {
                                if (newCode.isNotBlank()) {
                                    viewModel.createCoupon(
                                        code = newCode.trim(),
                                        discountType = discountType,
                                        discountValue = discountVal.toIntOrNull() ?: 50,
                                        maxRedemptions = 1000,
                                        expiry = expiryText
                                    )
                                    showCreateCouponModal = false
                                }
                            },
                            variant = ClayButtonVariant.PINK,
                            isDark = isDark
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save Coupon", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactVipPlanRowCard(
    plan: VipPlanItem,
    isDark: Boolean,
    textColor: Color,
    mutedColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val perkTags = buildList {
        if (plan.unlockVipTemplates) add("👑 VIP Tpl")
        if (plan.freeCustomLink) add("🔗 Free Link")
        if (plan.freeHdPhotos) add("🖼️ Free HD")
        if (plan.freeCustomStyle) add("🎨 Style")
        if (plan.customMusicVoice) add("🎵 Audio")
        if (plan.removeWatermark) add("✨ No WM")
        if (plan.lifetimeLink) add("♾️ Forever")
    }

    ClayWellBox(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClayPill(
                            text = plan.badge.ifBlank { "VIP PLAN" },
                            color = if (plan.isPopular) ClayColors.Pink else ClayColors.Primary,
                            isDark = isDark
                        )
                        Text(
                            text = if (plan.price == 0) "₹0 FREE" else "₹${plan.price}",
                            color = ClayColors.Amber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = plan.billingLabel,
                            color = mutedColor,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = plan.name,
                        color = textColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Icon-Only Edit & Delete Actions (Strict 30.dp bounds + 8.dp gap = zero overlap)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CompactIconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ClayColors.Pink.copy(alpha = 0.15f))
                            .border(1.dp, ClayColors.Pink.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Plan",
                            tint = ClayColors.Pink,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    CompactIconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ClayColors.Rose.copy(alpha = 0.15f))
                            .border(1.dp, ClayColors.Rose.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Plan",
                            tint = ClayColors.Rose,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (perkTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    perkTags.forEach { tag ->
                        ClayPill(text = tag, color = ClayColors.Emerald, isDark = isDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun VipPlanEditorDialog(
    initialPlan: VipPlanItem?,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (VipPlanItem) -> Unit
) {
    var name by remember(initialPlan) { mutableStateOf(initialPlan?.name ?: "") }
    var badge by remember(initialPlan) { mutableStateOf(initialPlan?.badge ?: "👑 VIP PASS") }
    var priceText by remember(initialPlan) { mutableStateOf((initialPlan?.price ?: 49).toString()) }
    var origPriceText by remember(initialPlan) { mutableStateOf((initialPlan?.originalPrice ?: 99).toString()) }
    var billingLabel by remember(initialPlan) { mutableStateOf(initialPlan?.billingLabel ?: "one-time") }
    var ctaText by remember(initialPlan) { mutableStateOf(initialPlan?.ctaText ?: "") }
    var description by remember(initialPlan) {
        mutableStateOf(
            initialPlan?.description
                ?: "Unlock VIP templates, custom link (/w/your-name), HD photos, custom styles & zero watermark."
        )
    }
    var isPopular by remember(initialPlan) { mutableStateOf(initialPlan?.isPopular ?: true) }

    var freeCustomLink by remember(initialPlan) { mutableStateOf(initialPlan?.freeCustomLink ?: true) }
    var freeHdPhotos by remember(initialPlan) { mutableStateOf(initialPlan?.freeHdPhotos ?: true) }
    var freeCustomStyle by remember(initialPlan) { mutableStateOf(initialPlan?.freeCustomStyle ?: true) }
    var unlockVipTemplates by remember(initialPlan) { mutableStateOf(initialPlan?.unlockVipTemplates ?: true) }
    var customMusicVoice by remember(initialPlan) { mutableStateOf(initialPlan?.customMusicVoice ?: true) }
    var removeWatermark by remember(initialPlan) { mutableStateOf(initialPlan?.removeWatermark ?: true) }
    var lifetimeLink by remember(initialPlan) { mutableStateOf(initialPlan?.lifetimeLink ?: true) }

    var featuresText by remember(initialPlan) {
        mutableStateOf(
            initialPlan?.features?.joinToString("\n")
                ?: "Unlock VIP & Premium 3D Templates\nFree Custom Link (/w/your-name) Included\nUp to 16 Uncompressed HD Photos\n100% Zero Watermark & Lifetime Link"
        )
    }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        TopAccentCard(
            accentBrush = Brush.horizontalGradient(listOf(ClayColors.Pink, ClayColors.Primary, ClayColors.Amber)),
            isDark = isDark,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialPlan == null) "Create New VIP Plan" else "Edit: ${initialPlan.name}",
                        color = textColor,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    CompactIconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = mutedColor)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 6.dp), color = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder)

                // Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1.2f)) {
                            ClayTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Plan Name *",
                                isDark = isDark
                            )
                        }
                        Box(modifier = Modifier.weight(0.8f)) {
                            ClayTextField(
                                value = badge,
                                onValueChange = { badge = it },
                                label = "Badge",
                                isDark = isDark
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = "Price (₹) * (0=Free)",
                                isDark = isDark
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = origPriceText,
                                onValueChange = { origPriceText = it },
                                label = "Original ₹",
                                isDark = isDark
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ClayTextField(
                                value = billingLabel,
                                onValueChange = { billingLabel = it },
                                label = "Billing",
                                isDark = isDark
                            )
                        }
                    }

                    ClayTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Short Description",
                        isDark = isDark,
                        singleLine = false
                    )

                    Text(
                        text = "🎁 Free Perks Included in Plan (Tap to Toggle):",
                        color = ClayColors.Pink,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    val perkItems = listOf(
                        Triple("🔗 Free Custom Link", freeCustomLink) { freeCustomLink = !freeCustomLink },
                        Triple("🖼️ Free HD Photos", freeHdPhotos) { freeHdPhotos = !freeHdPhotos },
                        Triple("🎨 Free Custom Style", freeCustomStyle) { freeCustomStyle = !freeCustomStyle },
                        Triple("👑 Unlock VIP Templates", unlockVipTemplates) { unlockVipTemplates = !unlockVipTemplates },
                        Triple("🎵 Music & Voice", customMusicVoice) { customMusicVoice = !customMusicVoice },
                        Triple("✨ Zero Watermark", removeWatermark) { removeWatermark = !removeWatermark },
                        Triple("♾️ Lifetime Link", lifetimeLink) { lifetimeLink = !lifetimeLink },
                        Triple("⭐ Popular Highlight", isPopular) { isPopular = !isPopular }
                    )

                    perkItems.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pair.forEach { (label, checked, toggle) ->
                                ClayButton(
                                    onClick = toggle,
                                    variant = if (checked) ClayButtonVariant.EMERALD else ClayButtonVariant.SECONDARY,
                                    isDark = isDark,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (checked) "✓ $label" else label,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    ClayTextField(
                        value = ctaText,
                        onValueChange = { ctaText = it },
                        label = "Button Text (Optional)",
                        isDark = isDark
                    )

                    ClayTextField(
                        value = featuresText,
                        onValueChange = { featuresText = it },
                        label = "Feature Bullets (One per line)",
                        isDark = isDark,
                        singleLine = false
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 6.dp), color = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder)

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = mutedColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    ClayButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                val pVal = (priceText.toIntOrNull() ?: 49).coerceAtLeast(0)
                                val oVal = (origPriceText.toIntOrNull() ?: pVal).coerceAtLeast(0)
                                val id = initialPlan?.id
                                    ?: ("vip_" + name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_') + "_" + (System.currentTimeMillis() % 1000))
                                val parsedFeatures = featuresText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                                onSave(
                                    VipPlanItem(
                                        id = id,
                                        name = name.trim(),
                                        badge = badge.trim().ifBlank { "👑 VIP PLAN" },
                                        price = pVal,
                                        originalPrice = oVal,
                                        billingLabel = billingLabel.trim().ifBlank { "one-time" },
                                        description = description.trim(),
                                        ctaText = ctaText.trim().ifBlank { if (pVal == 0) "Claim ${name.trim()} (FREE)" else "Unlock ${name.trim()} • ₹$pVal" },
                                        isPopular = isPopular,
                                        isActive = true,
                                        freeCustomLink = freeCustomLink,
                                        freeHdPhotos = freeHdPhotos,
                                        freeCustomStyle = freeCustomStyle,
                                        unlockVipTemplates = unlockVipTemplates,
                                        customMusicVoice = customMusicVoice,
                                        removeWatermark = removeWatermark,
                                        lifetimeLink = lifetimeLink,
                                        maxPhotos = if (freeHdPhotos) 16 else 3,
                                        validityDays = if (lifetimeLink) 0 else 30,
                                        features = parsedFeatures
                                    )
                                )
                            }
                        },
                        variant = ClayButtonVariant.PINK,
                        isDark = isDark
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save VIP Plan", fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}
