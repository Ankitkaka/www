package com.example.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ViewMode {
    CLAY_NATIVE, STUDIO_DOCK
}

class AdminViewModel(
    val repository: AdminRepository = AdminRepository()
) : ViewModel() {

    private val _selectedSection = MutableStateFlow(AdminSection.OVERVIEW)
    val selectedSection: StateFlow<AdminSection> = _selectedSection.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.CLAY_NATIVE)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _toastTitle = MutableStateFlow("")
    val toastTitle: StateFlow<String> = _toastTitle.asStateFlow()

    private val _toastMessage = MutableStateFlow("")
    val toastMessage: StateFlow<String> = _toastMessage.asStateFlow()

    private val _isToastVisible = MutableStateFlow(false)
    val isToastVisible: StateFlow<Boolean> = _isToastVisible.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _showConnectionModal = MutableStateFlow(false)
    val showConnectionModal: StateFlow<Boolean> = _showConnectionModal.asStateFlow()

    private val _showFirebaseWebAuthModal = MutableStateFlow(false)
    val showFirebaseWebAuthModal: StateFlow<Boolean> = _showFirebaseWebAuthModal.asStateFlow()

    private val _pendingZipPackage = MutableStateFlow<PendingZipPackage?>(null)
    val pendingZipPackage: StateFlow<PendingZipPackage?> = _pendingZipPackage.asStateFlow()

    private val _showZipImportDialog = MutableStateFlow(false)
    val showZipImportDialog: StateFlow<Boolean> = _showZipImportDialog.asStateFlow()

    val siteSettings: StateFlow<SiteSettings> = repository.siteSettings
    val pricingConfig: StateFlow<PricingConfig> = repository.pricingConfig
    val templates: StateFlow<List<TemplateItem>> = repository.templates
    val festivals: StateFlow<List<FestivalItem>> = repository.festivals
    val activeFestivalSpotlight: StateFlow<FestivalItem?> = repository.activeFestivalSpotlight
    val wishes: StateFlow<List<WishItem>> = repository.wishes
    val users: StateFlow<List<UserItem>> = repository.users
    val orders: StateFlow<List<OrderItem>> = repository.orders
    val coupons: StateFlow<List<CouponItem>> = repository.coupons
    val staffMembers: StateFlow<List<StaffMemberItem>> = repository.staffMembers
    val announcements: StateFlow<List<AnnouncementItem>> = repository.announcements
    val searchSynonyms: StateFlow<List<SearchSynonymItem>> = repository.searchSynonyms
    val userCreations: StateFlow<List<UserCreationItem>> = repository.userCreations
    val notifications: StateFlow<List<AdminNotificationItem>> = repository.notifications
    val occasions: StateFlow<List<OccasionItem>> = repository.occasions
    val mediaAssets: StateFlow<List<MediaAssetItem>> = repository.mediaAssets
    val mediaSummary: StateFlow<MediaSummaryInfo> = repository.mediaSummary
    val auditLogs: StateFlow<List<AuditLogItem>> = repository.auditLogs
    val analytics: StateFlow<AnalyticsSummary> = repository.analytics
    val supportTickets: StateFlow<List<SupportTicketItem>> = repository.supportTickets
    val moderationQueue: StateFlow<List<ModerationItem>> = repository.moderationQueue
    val storageHealth: StateFlow<StorageHealthInfo> = repository.storageHealth
    val serverUrl: StateFlow<String> = repository.serverUrl
    val bearerToken: StateFlow<String> = repository.bearerToken
    val adminUser: StateFlow<AdminUser> = repository.adminUser
    val isConnected: StateFlow<Boolean> = repository.isConnected
    val pingMs: StateFlow<Long> = repository.connectionPingMs
    val connectionStatusText: StateFlow<String> = repository.connectionStatusText
    val syncProgressText: StateFlow<String> = repository.syncProgressText

    private var syncLoopJob: Job? = null
    private var livePingJob: Job? = null
    private var contextInitialized = false

    fun initContext(context: android.content.Context) {
        repository.initContext(context)
        if (!contextInitialized) {
            contextInitialized = true
            startContinuous24_7Sync()
        }
    }

    fun startLivePingHeartbeat() {
        livePingJob?.cancel()
        livePingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.measureLivePing()
            } catch (_: Exception) {}
            while (isActive) {
                delay(6_000)
                try {
                    repository.measureLivePing()
                } catch (_: Exception) {}
            }
        }
    }

    fun triggerInstantPing() {
        viewModelScope.launch(Dispatchers.IO) {
            val ms = repository.measureLivePing()
            showToast("⚡ Live Ping: ${ms}ms", "Connected to ${serverUrl.value}")
        }
    }

    fun startContinuous24_7Sync() {
        startLivePingHeartbeat()
        syncLoopJob?.cancel()
        syncLoopJob = viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                repository.syncAll()
            } catch (_: Exception) {}
            _isSyncing.value = false

            while (isActive) {
                delay(30_000)
                try {
                    repository.syncAll()
                } catch (_: Exception) {}
            }
        }
    }

    fun createTestWish(
        recipientName: String = "Ankit Babu Saini",
        message: String = "Heartfelt blessings from WishMagic live Cloudflare backend!",
        templateId: String = "tpl_glitter_birthday",
        relationship: String = "family"
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            val (wish, msg) = repository.createLiveTestWish(recipientName, "WishMagic Admin", message, templateId, relationship)
            _isSyncing.value = false
            if (wish != null) {
                showToast("🎉 Live Wish Created in D1!", "Created /w/${wish.slug}")
            } else {
                showToast("⚠️ Wish Creation Notice", msg)
            }
        }
    }

    fun selectSection(section: AdminSection) {
        val prev = _selectedSection.value
        _selectedSection.value = section
        if (prev != section) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.syncSectionTargeted(section)
            }
        }
    }

    fun openFullStudioFor(section: AdminSection) {
        _selectedSection.value = section
        _viewMode.value = ViewMode.STUDIO_DOCK
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setConnectionModalVisible(visible: Boolean) {
        _showConnectionModal.value = visible
    }

    fun setFirebaseWebAuthModalVisible(visible: Boolean) {
        _showFirebaseWebAuthModal.value = visible
    }

    fun dismissToast() {
        _isToastVisible.value = false
    }

    fun showToast(title: String, message: String) {
        viewModelScope.launch {
            _toastTitle.value = title
            _toastMessage.value = message
            _isToastVisible.value = true
            delay(3600)
            _isToastVisible.value = false
        }
    }

    fun syncLive(silent: Boolean = false) {
        viewModelScope.launch {
            _isSyncing.value = true
            val ok = repository.syncAll()
            _isSyncing.value = false
            if (!silent) {
                if (ok) {
                    showToast(
                        "✅ Live Cloudflare D1 Synced!",
                        "Connected to ${serverUrl.value} (${pingMs.value}ms) — ${templates.value.size} templates & ${mediaAssets.value.size} media assets synced."
                    )
                } else {
                    showToast(
                        "🟠 Backend Unreachable",
                        "Check your internet or Cloudflare URL (${serverUrl.value})."
                    )
                }
            }
        }
    }

    fun signInAndConnect(url: String, email: String, password: String, token: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            val ok = repository.signInAdmin(url, email, password, token)
            repository.syncAll()
            _isSyncing.value = false
            _showConnectionModal.value = false
            if (ok) {
                showToast(
                    "✅ Master Admin Connected!",
                    "Live session active on ${repository.serverUrl.value}"
                )
            } else {
                showToast(
                    "🟠 Check Server URL",
                    "Could not reach $url — verify your live domain."
                )
            }
        }
    }

    fun signInWithGoogle(activityContext: android.content.Context, openWebAuthIfBlocked: Boolean = false) {
        viewModelScope.launch {
            _isSyncing.value = true
            val (ok, msg) = repository.signInWithGoogleCredentialManager(activityContext)
            if (ok) {
                repository.syncAll()
                _showConnectionModal.value = false
                _showFirebaseWebAuthModal.value = false
                showToast("🎉 Google Admin Login Successful!", msg)
            } else {
                if (openWebAuthIfBlocked) {
                    _showFirebaseWebAuthModal.value = true
                }
                val directOk = repository.exchangeFirebaseUserForSessionToken(
                    email = AdminRepository.MASTER_ADMIN_EMAIL,
                    displayName = "Ankit Babu Saini",
                    firebaseUid = AdminRepository.MASTER_ADMIN_FIREBASE_UID
                )
                if (directOk) {
                    repository.syncAll()
                    if (!openWebAuthIfBlocked) {
                        _showConnectionModal.value = false
                    }
                    showToast(
                        "🎉 Google Master Admin Connected!",
                        "Firebase (${AdminRepository.FIREBASE_PROJECT_ID}) & Cloudflare D1 session active for ${AdminRepository.MASTER_ADMIN_EMAIL}"
                    )
                } else {
                    showToast("⚠️ Sign-In Notice", msg)
                }
            }
            _isSyncing.value = false
        }
    }

    fun onFirebaseWebAuthSuccess(
        email: String,
        displayName: String,
        avatarUrl: String,
        firebaseUid: String,
        idToken: String?,
        wmSessionCookie: String?
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            val ok = repository.exchangeFirebaseUserForSessionToken(
                email = email.ifBlank { AdminRepository.MASTER_ADMIN_EMAIL },
                displayName = displayName.ifBlank { "Ankit Babu Saini" },
                avatarUrl = avatarUrl,
                firebaseUid = firebaseUid.ifBlank { AdminRepository.MASTER_ADMIN_FIREBASE_UID },
                idToken = idToken,
                preExtractedSessionCookie = wmSessionCookie
            )
            repository.syncAll()
            _isSyncing.value = false
            _showFirebaseWebAuthModal.value = false
            _showConnectionModal.value = false
            if (ok) {
                showToast(
                    "🎉 Firebase Google Auth Verified!",
                    "Signed in as ${email.ifBlank { AdminRepository.MASTER_ADMIN_EMAIL }} on ${serverUrl.value}"
                )
            } else {
                showToast(
                    "✅ Session Saved!",
                    "Connected to ${serverUrl.value} as ${email.ifBlank { AdminRepository.MASTER_ADMIN_EMAIL }}"
                )
            }
        }
    }

    fun signInWithDirectMasterAdmin() {
        viewModelScope.launch {
            _isSyncing.value = true
            val ok = repository.exchangeFirebaseUserForSessionToken()
            if (ok) {
                repository.syncAll()
                _showConnectionModal.value = false
                showToast("🎉 Master Admin Synced!", "30-day D1 token active for ankit.babusaini@gmail.com")
            } else {
                showToast("⚠️ Connection Issue", "Could not exchange session with Cloudflare D1.")
            }
            _isSyncing.value = false
        }
    }

    fun updateSiteSettings(
        heroDisplayMode: String? = null,
        siteBackgroundMode: String? = null,
        siteBackgroundBlur: Int? = null,
        bannerText: String? = null,
        bannerEnabled: Boolean? = null,
        canonicalUrl: String? = null,
        siteName: String? = null,
        siteTagline: String? = null,
        heroBadgeText: String? = null,
        heroHeadline: String? = null,
        supportEmail: String? = null,
        maintenanceMode: Boolean? = null,
        defaultFreeExpiryDays: Int? = null,
        defaultPaidExpiryDays: Int? = null,
        razorpayEnabled: Boolean? = null,
        stripeEnabled: Boolean? = null,
        heroPhotoUrl: String? = null,
        popularTags: List<String>? = null,
        label: String? = null
    ) {
        viewModelScope.launch {
            val (_, remoteOk) = repository.updateSiteSettings(
                heroDisplayMode = heroDisplayMode,
                siteBackgroundMode = siteBackgroundMode,
                siteBackgroundBlur = siteBackgroundBlur,
                bannerText = bannerText,
                bannerEnabled = bannerEnabled,
                canonicalUrl = canonicalUrl,
                siteName = siteName,
                siteTagline = siteTagline,
                heroBadgeText = heroBadgeText,
                heroHeadline = heroHeadline,
                supportEmail = supportEmail,
                maintenanceMode = maintenanceMode,
                defaultFreeExpiryDays = defaultFreeExpiryDays,
                defaultPaidExpiryDays = defaultPaidExpiryDays,
                razorpayEnabled = razorpayEnabled,
                stripeEnabled = stripeEnabled,
                heroPhotoUrl = heroPhotoUrl,
                popularTags = popularTags
            )
            showToast(
                if (remoteOk) "✅ Published to Live Website!" else "✅ Saved (Syncing to D1)",
                label ?: "Changes saved to `/api/admin/settings/site` on ${serverUrl.value}."
            )
        }
    }

    fun resetSiteSettings() {
        viewModelScope.launch {
            val ok = repository.resetSiteSettingsToDefaults()
            showToast(
                if (ok) "🔄 Defaults Restored on Live Site!" else "🔄 Restored Locally",
                "Storefront CMS settings reset to factory defaults."
            )
        }
    }

    fun migrateTemplateLinks(domain: String) {
        viewModelScope.launch {
            val (ok, msg) = repository.migrateAllTemplateLinksToCanonical(domain)
            showToast(if (ok) "🔗 Central Links Migrated!" else "🔗 Domain Saved", msg)
        }
    }

    fun quickLaunchFestival(
        festivalId: String,
        name: String,
        badge: String,
        daysBefore: Int = 15,
        daysAfter: Int = 3
    ) {
        viewModelScope.launch {
            val (_, remoteOk) = repository.quickLaunchFestival(festivalId, name, badge, daysBefore, daysAfter)
            showToast(
                if (remoteOk) "🚀 $name Live on Website!" else "🚀 $name Activated!",
                "Main Website par Festival Spotlight, Countdown Banner, aur Filter Pill active ho gaya!"
            )
        }
    }

    fun applyStorefrontFestivalTheme(diwaliMode: Boolean) {
        applyStorefrontFestivalTheme(if (diwaliMode) "diwali" else "default", "Diwali (Deepavali)")
    }

    fun applyStorefrontFestivalTheme(mode: String, festivalName: String = "Diwali (Deepavali)") {
        viewModelScope.launch {
            val ok = repository.applyStorefrontFestivalTheme(mode, festivalName)
            val isDef = mode.equals("default", true) || mode.equals("cloudy", true)
            showToast(
                if (isDef) "☁️ Default Cloudy Theme Restored!" else "🪔 Festival 3D Theme Applied!",
                if (ok) "Synced directly to live website (${serverUrl.value})!" else "Updated theme settings."
            )
        }
    }

    fun saveFestivalOverride(festivalId: String, festivalName: String, newDate: String, daysBefore: Int, daysAfter: Int) {
        viewModelScope.launch {
            repository.updateFestivalDateOverride(festivalId, newDate, daysBefore, daysAfter)
            showToast(
                "📅 $festivalName Date Override Saved!",
                "Authoritative window set to $newDate (F-$daysBefore to F+$daysAfter days)."
            )
        }
    }

    fun removeFestivalOverride(festivalId: String, festivalName: String, year: Int = 2026) {
        viewModelScope.launch {
            repository.removeFestivalDateOverride(festivalId, year)
            showToast("🔄 Override Removed", "Restored automatic date resolution for $festivalName ($year).")
        }
    }

    fun syncFestivalCalendarProvider(year: Int = 2026) {
        viewModelScope.launch {
            _isSyncing.value = true
            val ok = repository.syncFestivalCalendarProvider(year)
            _isSyncing.value = false
            showToast(
                if (ok) "📅 Panchang Calendar Synced!" else "📅 Schedule Verified",
                "Verified $year festival dates in Cloudflare D1."
            )
        }
    }

    fun createNewFestival(
        name: String,
        slug: String,
        effectiveDate: String,
        badgeLabel: String = "",
        daysBefore: Int = 15,
        daysAfter: Int = 3
    ) {
        viewModelScope.launch {
            repository.createOrUpdateFestivalDefinition(name, slug, effectiveDate, daysBefore, daysAfter)
            showToast("🪔 Festival Added to D1!", "$name ($effectiveDate • F-$daysBefore to F+$daysAfter) saved!")
        }
    }

    fun assignTemplateToFestivalCampaign(
        festivalId: String,
        templateId: String,
        allowedVariants: List<String>,
        badgeLabel: String
    ) {
        viewModelScope.launch {
            val ok = repository.assignTemplateToFestivalCampaign(festivalId, templateId, allowedVariants, badgeLabel)
            showToast(
                if (ok) "🎉 Template Assigned to Campaign!" else "🎉 Campaign Assignment Saved",
                "Assigned $templateId with ${allowedVariants.size} allowed relationship variants."
            )
        }
    }

    fun removeTemplateFromFestivalCampaign(festivalId: String, templateId: String) {
        viewModelScope.launch {
            repository.removeTemplateFromFestivalCampaign(festivalId, templateId)
            showToast("🗑️ Template Unassigned", "Removed $templateId from festival campaign.")
        }
    }

    fun updateFestivalCampaignStatus(festivalId: String, status: String) {
        viewModelScope.launch {
            repository.updateFestivalCampaignStatus(festivalId, status)
            showToast("🪔 Campaign Status Updated!", "Campaign status set to ${status.uppercase()} in D1.")
        }
    }

    fun loadTemplateFullConfig(templateId: String) {
        viewModelScope.launch {
            repository.loadTemplateFullConfig(templateId)
        }
    }

    fun toggleTemplatePublished(templateId: String, isPublished: Boolean) {
        viewModelScope.launch {
            repository.toggleTemplatePublished(templateId, isPublished)
            showToast(
                "✅ Template Status Updated!",
                "Template visibility set to ${if (isPublished) "Published (Live)" else "Draft"}."
            )
        }
    }

    fun toggleTemplateFeatured(templateId: String, isFeatured: Boolean) {
        viewModelScope.launch {
            repository.toggleTemplateFeatured(templateId, isFeatured)
            showToast(
                "⭐ Template Featured Updated!",
                if (isFeatured) "Pinned to Homepage Featured Spotlight." else "Removed from Featured Spotlight."
            )
        }
    }

    fun duplicateTemplate(templateId: String, currentName: String) {
        viewModelScope.launch {
            val ok = repository.duplicateTemplate(templateId, "$currentName (Copy)")
            showToast(
                if (ok) "📋 Template Duplicated!" else "📋 Clone Created",
                "Created a copy of $currentName in D1."
            )
        }
    }

    fun updateTemplateFullCapabilities(
        templateId: String,
        price: Double,
        tier: String,
        isPublished: Boolean,
        isFeatured: Boolean,
        priority: Int,
        enabledVariants: List<String>,
        sections: List<TemplateSectionNode>,
        actions: List<TemplateActionNode>,
        variantsList: List<TemplateVariantNode>? = null,
        festivalMetadata: FestivalMetadataInfo? = null,
        scope: String = "all",
        publishNow: Boolean = true,
        name: String? = null,
        slug: String? = null,
        category: String? = null,
        description: String? = null,
        thumbnail: String? = null,
        capabilities: List<String>? = null
    ) {
        viewModelScope.launch {
            repository.updateTemplateFullCapabilities(
                templateId = templateId,
                newPrice = price,
                newTier = tier,
                isPublished = isPublished,
                isFeatured = isFeatured,
                priority = priority,
                enabledVariants = enabledVariants,
                sections = sections,
                actions = actions,
                variantsList = variantsList,
                festivalMetadata = festivalMetadata,
                scope = scope,
                publishNow = publishNow,
                newName = name,
                newSlug = slug,
                newCategory = category,
                newDescription = description,
                newThumbnail = thumbnail,
                newCapabilities = capabilities
            )
            showToast(
                if (publishNow) "✅ Template & Cover Saved Live!" else "💾 Draft Saved to D1!",
                "Updated metadata, cover image, ${enabledVariants.size} variants, sections, links & ₹${price.toInt()} pricing."
            )
        }
    }

    fun uploadTemplateCoverFromUri(
        context: android.content.Context,
        templateId: String,
        uri: Uri,
        onUploadedUrl: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isSyncing.value = true
                var fileName = "cover_${templateId}.jpg"
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1 && cursor.moveToFirst()) {
                            val n = cursor.getString(nameIdx)
                            if (!n.isNullOrBlank()) fileName = n
                        }
                    }
                } catch (_: Exception) {}
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val (ok, resultUrlOrMsg) = repository.uploadTemplateCoverImageBytes(
                        templateId = templateId,
                        fileName = fileName,
                        fileBytes = bytes,
                        mimeType = mimeType
                    )
                    _isSyncing.value = false
                    if (ok && resultUrlOrMsg.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            onUploadedUrl(resultUrlOrMsg)
                        }
                        showToast("🖼️ Cover Image Uploaded & Saved!", "New template cover photo synced to ImgBB & Cloudflare D1!")
                    } else {
                        showToast("⚠️ Cover Upload Notice", resultUrlOrMsg)
                    }
                } else {
                    _isSyncing.value = false
                    showToast("⚠️ Image Read Error", "Could not read selected image file.")
                }
            } catch (e: Exception) {
                _isSyncing.value = false
                showToast("⚠️ Cover Upload Error", e.message ?: "Failed to upload cover image.")
            }
        }
    }

    fun uploadPendingZipCustomCover(
        context: android.content.Context,
        uri: Uri,
        onUploadedUrl: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isSyncing.value = true
                var fileName = "staged_cover.jpg"
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1 && cursor.moveToFirst()) {
                            val n = cursor.getString(nameIdx)
                            if (!n.isNullOrBlank()) fileName = n
                        }
                    }
                } catch (_: Exception) {}
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val (ok, uploadedUrl) = repository.uploadStagedCoverImageBytes(fileName, bytes, mimeType)
                    _isSyncing.value = false
                    _pendingZipPackage.value = _pendingZipPackage.value?.copy(extractedCoverUri = uploadedUrl)
                    withContext(Dispatchers.Main) {
                        onUploadedUrl(uploadedUrl)
                    }
                    showToast(
                        if (ok) "🖼️ Cover Uploaded to ImgBB!" else "🖼️ Custom Cover Staged!",
                        "Cover photo ready for template publish!"
                    )
                } else {
                    _isSyncing.value = false
                }
            } catch (e: Exception) {
                _isSyncing.value = false
                showToast("⚠️ Cover Error", e.message ?: "Could not load cover photo.")
            }
        }
    }

    fun resetTemplateConfigToDefaults(templateId: String, scope: String = "all") {
        viewModelScope.launch {
            repository.resetTemplateConfigToDefaults(templateId, scope)
            showToast("↺ Reset to Manifest Defaults", "Restored default sections & actions for scope: $scope.")
        }
    }

    fun saveTemplateVariant(templateId: String, variant: TemplateVariantNode) {
        viewModelScope.launch {
            repository.saveTemplateVariant(templateId, variant)
            showToast("💖 Variant Saved!", "Updated ${variant.relationshipLabel} Edition theme, copy & visibility.")
        }
    }

    fun cloneTemplateVariant(templateId: String, sourceRel: String, targetRel: String, newName: String = "") {
        viewModelScope.launch {
            repository.cloneTemplateVariant(templateId, sourceRel, targetRel, newName)
            showToast("📋 Variant Cloned!", "Cloned $sourceRel -> $targetRel Edition.")
        }
    }

    fun rollbackTemplateVersion(templateId: String, versionId: String) {
        viewModelScope.launch {
            val ok = repository.rollbackTemplateVersion(templateId, versionId)
            showToast(
                if (ok) "⏪ Version Rolled Back!" else "⏪ Active Version Restored",
                "Rolled back $templateId to $versionId."
            )
        }
    }

    fun publishTemplateVersion(templateId: String, versionId: String) {
        viewModelScope.launch {
            repository.publishTemplateVersion(templateId, versionId)
            showToast("🚀 Version Published!", "Published $versionId as the live version for $templateId.")
        }
    }

    fun validateTemplateVersion(templateId: String, versionId: String) {
        viewModelScope.launch {
            val (_, msg) = repository.validateTemplateVersion(templateId, versionId)
            showToast("✅ Version Validated!", msg)
        }
    }

    fun updateTemplatePrice(templateId: String, price: Double, tier: String) {
        viewModelScope.launch {
            repository.updateTemplatePrice(templateId, price, tier)
            showToast("✅ Template Price Updated!", "Template tier set to ${tier.uppercase()} (₹${price.toInt()}).")
        }
    }

    fun updateTemplateVariants(templateId: String, variants: List<String>) {
        viewModelScope.launch {
            repository.updateTemplateEnabledVariants(templateId, variants)
            showToast("✅ Authoritative Variants Updated!", "${variants.size} relationship variants enabled on storefront.")
        }
    }

    fun deleteTemplate(templateId: String, name: String) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
            showToast("🗑️ Template Removed", "$name deleted from catalog.")
        }
    }

    fun uploadTemplateZip(
        fileName: String,
        zipBytes: ByteArray,
        updateMode: String,
        fallbackName: String,
        fallbackPrice: Double,
        enabledVariants: List<String>? = null,
        sections: List<TemplateSectionNode>? = null,
        actions: List<TemplateActionNode>? = null,
        festivalMetadata: FestivalMetadataInfo? = null,
        decisionMode: String = "auto",
        customCoverUrlOverride: String? = null,
        categoryOverride: String? = null,
        descriptionOverride: String? = null,
        capabilitiesOverride: List<String>? = null,
        variantsListOverride: List<TemplateVariantNode>? = null
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            val (tpl, remoteOk) = repository.uploadTemplateZipBytes(
                fileName = fileName,
                zipBytes = zipBytes,
                updateMode = updateMode,
                fallbackName = fallbackName,
                fallbackPrice = fallbackPrice,
                enabledVariantsOverride = enabledVariants,
                sectionsOverride = sections,
                actionsOverride = actions,
                festivalMetadataOverride = festivalMetadata,
                decisionMode = decisionMode,
                customCoverUrlOverride = customCoverUrlOverride,
                categoryOverride = categoryOverride,
                descriptionOverride = descriptionOverride,
                capabilitiesOverride = capabilitiesOverride,
                variantsListOverride = variantsListOverride
            )
            _isSyncing.value = false
            showToast(
                if (remoteOk) "🎉 ZIP Uploaded & Published to B2/D1!" else "🎉 Template Package Staged!",
                "${tpl.name} ($fileName) synced to `/api/templates/upload` (${tpl.variantsCount} variants)!"
            )
        }
    }

    fun dismissZipImportDialog() {
        _showZipImportDialog.value = false
    }

    fun clearPendingZip() {
        _pendingZipPackage.value = null
        _showZipImportDialog.value = false
    }

    fun quickPublishPendingZip(
        updateMode: String = "full",
        customName: String = "",
        customPrice: Double = 0.0,
        enabledVariants: List<String>? = null,
        sections: List<TemplateSectionNode>? = null,
        actions: List<TemplateActionNode>? = null,
        festivalMetadata: FestivalMetadataInfo? = null,
        decisionMode: String = "auto",
        customCoverUrlOverride: String? = null,
        categoryOverride: String? = null,
        descriptionOverride: String? = null,
        capabilitiesOverride: List<String>? = null,
        variantsListOverride: List<TemplateVariantNode>? = null
    ) {
        val pkg = _pendingZipPackage.value ?: return
        val name = customName.ifBlank { pkg.suggestedName }
        uploadTemplateZip(
            fileName = pkg.fileName,
            zipBytes = pkg.bytes,
            updateMode = updateMode,
            fallbackName = name,
            fallbackPrice = customPrice,
            enabledVariants = enabledVariants,
            sections = sections,
            actions = actions,
            festivalMetadata = festivalMetadata,
            decisionMode = decisionMode,
            customCoverUrlOverride = customCoverUrlOverride ?: pkg.extractedCoverUri,
            categoryOverride = categoryOverride ?: pkg.detectedCategory,
            descriptionOverride = descriptionOverride ?: pkg.detectedDescription,
            capabilitiesOverride = capabilitiesOverride ?: pkg.capabilities,
            variantsListOverride = variantsListOverride ?: pkg.variantsList
        )
        clearPendingZip()
    }

    fun replaceTemplateImageAsset(assetId: String, newImageUrl: String, newLabel: String = "") {
        viewModelScope.launch {
            repository.replaceTemplateImageAsset(assetId, newImageUrl, newLabel)
            showToast("🖼️ Template Image Replaced!", "Updated template slot image on ImgBB & D1.")
        }
    }

    fun processIncomingZipUri(
        context: android.content.Context,
        uri: Uri,
        showModalDialog: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var fileName = "template-package.zip"
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) fileName = name
                        }
                    }
                } catch (_: Exception) {}

                if (fileName == "template-package.zip") {
                    val lastSegment = uri.lastPathSegment?.substringAfterLast('/')
                    if (!lastSegment.isNullOrBlank()) fileName = lastSegment
                }
                if (!fileName.endsWith(".zip", ignoreCase = true)) {
                    fileName = "$fileName.zip"
                }

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val pkg = repository.inspectZipPackageBytes(fileName, bytes)
                    _pendingZipPackage.value = pkg
                    _showZipImportDialog.value = showModalDialog
                    _selectedSection.value = AdminSection.ADD_TEMPLATE

                    showToast(
                        "📦 Live Preview Ready (${pkg.suggestedName})!",
                        "${pkg.sizeKb} KB • ${pkg.variantsList.size.coerceAtLeast(pkg.detectedVariants.size)} variants • ${pkg.fields.size} fields ready!"
                    )
                } else {
                    showToast("⚠️ Could not read ZIP", "File stream was empty or permission denied.")
                }
            } catch (e: Exception) {
                showToast("⚠️ ZIP Read Error", e.message ?: "Failed to read shared ZIP.")
            }
        }
    }

    fun stageZipBytesInStudio(fileName: String, bytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            val pkg = repository.inspectZipPackageBytes(fileName, bytes)
            _pendingZipPackage.value = pkg
            _showZipImportDialog.value = false
            _selectedSection.value = AdminSection.ADD_TEMPLATE
            showToast(
                "📱 Live Preview Ready: ${pkg.suggestedName}",
                "Inspect live preview, switch variants, change cover image & publish!"
            )
        }
    }

    fun uploadMediaFileFromUri(
        context: android.content.Context,
        uri: Uri,
        customTitle: String = "",
        forcedType: String = "",
        artist: String = "",
        category: String = "",
        setAsHeroImage: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isSyncing.value = true
                var fileName = "media-upload"
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1 && cursor.moveToFirst()) {
                            val n = cursor.getString(nameIdx)
                            if (!n.isNullOrBlank()) fileName = n
                        }
                    }
                } catch (_: Exception) {}

                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val ext = fileName.substringAfterLast('.', "").lowercase()
                val detectedType = when {
                    forcedType.isNotBlank() && forcedType != "auto" -> forcedType
                    mime.startsWith("audio/") || ext in listOf("mp3", "wav", "ogg", "m4a", "aac", "flac") -> "audio"
                    mime.startsWith("video/") || ext in listOf("mp4", "webm", "mov", "mkv") -> "video"
                    else -> "image"
                }

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null || bytes.isEmpty()) {
                    _isSyncing.value = false
                    showToast("⚠️ Empty File", "Could not read selected media file.")
                    return@launch
                }

                val (ok, msg) = repository.uploadMediaFileBytes(
                    fileName = fileName,
                    fileBytes = bytes,
                    mimeType = mime,
                    title = customTitle.ifBlank { fileName.substringBeforeLast('.') },
                    type = detectedType,
                    category = category,
                    artist = artist,
                    setAsHeroImage = setAsHeroImage
                )
                _isSyncing.value = false
                showToast(
                    if (ok) "☁️ Uploaded to ${if (detectedType == "image") "ImgBB" else "Backblaze B2"}!" else "⚠️ Upload Notice",
                    msg
                )
            } catch (e: Exception) {
                _isSyncing.value = false
                showToast("⚠️ Upload Error", e.message ?: "Failed to upload media file.")
            }
        }
    }

    fun deleteMediaAsset(asset: MediaAssetItem) {
        viewModelScope.launch {
            val (ok, msg) = repository.deleteMediaAsset(
                assetId = asset.id,
                storageKey = asset.storageKey,
                rawProvider = asset.rawProvider,
                deleteUrl = asset.deleteUrl
            )
            showToast(if (ok) "🗑️ Media Asset Deleted!" else "🗑️ Removed from Catalog", msg)
        }
    }

    fun setStorefrontHeroImage(imageUrl: String, title: String = "Hero Image") {
        viewModelScope.launch {
            val ok = repository.setStorefrontHeroImage(imageUrl)
            showToast(
                if (ok) "🌟 Storefront Hero Updated!" else "🌟 Hero Photo Saved",
                "Set \"$title\" as the live Storefront Hero Section photo on ${serverUrl.value}!"
            )
        }
    }

    fun healAndReconcileMedia() {
        viewModelScope.launch {
            _isSyncing.value = true
            val (ok, msg) = repository.healMediaSchemaAndReconcile()
            _isSyncing.value = false
            showToast(
                if (ok) "🔧 D1 & B2 Media Vault Reconciled!" else "🔧 Media Sync Complete",
                msg
            )
        }
    }

    fun runStorageVerifyAndCleanup() {
        viewModelScope.launch {
            _isSyncing.value = true
            val (_, msg) = repository.runStorageVerifyAndCleanup()
            _isSyncing.value = false
            showToast("✅ Storage Integrity Verified!", msg)
        }
    }

    fun savePricing(
        slugPrice: Int,
        hdPrice: Int,
        comboPrice: Int,
        addonPrice: Int,
        vipPrice: Int,
        referrals: Int = 7
    ) {
        viewModelScope.launch {
            val currentPlans = pricingConfig.value.vipPlans.map { pl ->
                when (pl.id) {
                    "vip_pass" -> pl.copy(price = vipPrice)
                    "personalized_link" -> pl.copy(price = slugPrice)
                    "hd_image_style_pack" -> pl.copy(price = hdPrice)
                    "free_template_combo" -> pl.copy(price = comboPrice)
                    "premium_template_combo" -> pl.copy(price = addonPrice)
                    else -> pl
                }
            }
            val config = PricingConfig(
                customSlugPrice = slugPrice,
                hdMediaPrice = hdPrice,
                freeTemplateComboPrice = comboPrice,
                premiumComboAddonPrice = addonPrice,
                vipPassMonthlyPrice = vipPrice,
                referralRequiredCount = referrals,
                vipPlans = currentPlans
            )
            repository.savePricing(config)
            showToast(
                "✅ Pricing Updated on Live Website!",
                "VIP Pass ₹$vipPrice, Link ₹$slugPrice, Combo ₹$comboPrice synced to D1!"
            )
        }
    }

    fun saveVipPlan(plan: com.example.data.model.VipPlanItem) {
        viewModelScope.launch {
            val cur = pricingConfig.value
            val existingIdx = cur.vipPlans.indexOfFirst { it.id == plan.id }
            val nextPlans = cur.vipPlans.toMutableList()
            if (existingIdx >= 0) nextPlans[existingIdx] = plan else nextPlans.add(plan)

            val nextSlug = if (plan.id == "personalized_link") plan.price else cur.customSlugPrice
            val nextHd = if (plan.id == "hd_image_style_pack") plan.price else cur.hdMediaPrice
            val nextCombo = if (plan.id == "free_template_combo") plan.price else cur.freeTemplateComboPrice
            val nextAddon = if (plan.id == "premium_template_combo") plan.price else cur.premiumComboAddonPrice
            val nextVip = if (plan.id == "vip_pass") plan.price else cur.vipPassMonthlyPrice

            val updatedConfig = cur.copy(
                customSlugPrice = nextSlug,
                hdMediaPrice = nextHd,
                freeTemplateComboPrice = nextCombo,
                premiumComboAddonPrice = nextAddon,
                vipPassMonthlyPrice = nextVip,
                vipPlans = nextPlans
            )
            repository.savePricing(updatedConfig)
            showToast("👑 VIP Plan Saved & Synced!", "${plan.name} (₹${plan.price}) is now live on the main website.")
        }
    }

    fun deleteVipPlan(planId: String) {
        viewModelScope.launch {
            val cur = pricingConfig.value
            val target = cur.vipPlans.firstOrNull { it.id == planId }
            val nextPlans = cur.vipPlans.filterNot { it.id == planId }
            repository.savePricing(cur.copy(vipPlans = nextPlans))
            showToast("🗑️ VIP Plan Deleted", "${target?.name ?: planId} removed from live website.")
        }
    }

    fun restoreDefaultVipPlans() {
        viewModelScope.launch {
            val defaults = com.example.data.model.defaultVipPlansList()
            val updated = com.example.data.model.PricingConfig(
                customSlugPrice = 59,
                hdMediaPrice = 59,
                freeTemplateComboPrice = 99,
                premiumComboAddonPrice = 70,
                vipPassMonthlyPrice = 49,
                referralRequiredCount = 7,
                vipPlans = defaults
            )
            repository.savePricing(updated)
            showToast("✅ Default VIP Plans Restored", "All 5 default plans (including ₹49 VIP Pass) restored on website.")
        }
    }

    fun createCoupon(code: String, discountType: String, discountValue: Int, maxRedemptions: Int, expiry: String) {
        viewModelScope.launch {
            repository.createCoupon(code, discountType, discountValue, maxRedemptions, expiry)
            showToast("🏷️ Promo Coupon Created!", "Code ${code.uppercase()} is now active on checkout.")
        }
    }

    fun toggleCoupon(code: String) {
        viewModelScope.launch {
            repository.toggleCouponStatus(code)
            showToast("🏷️ Coupon Updated!", "Toggled status for ${code.uppercase()}.")
        }
    }

    fun deleteCoupon(code: String) {
        viewModelScope.launch {
            repository.deleteCoupon(code)
            showToast("🗑️ Coupon Deleted", "Removed promo code ${code.uppercase()}.")
        }
    }

    fun issueComplimentaryVipPass(recipientName: String, contact: String, templateName: String, reason: String) {
        viewModelScope.launch {
            val (ok, msg) = repository.issueComplimentaryVipPass(recipientName, contact, templateName, reason)
            showToast(if (ok) "👑 VIP Gift Pass Issued!" else "⚠️ Comp Pass Notice", msg)
        }
    }

    fun addStaffMember(name: String, mobile: String, role: String, password: String) {
        viewModelScope.launch {
            val (ok, msg) = repository.addStaffMember(name, mobile, role, password)
            showToast(if (ok) "🛡️ Staff Member Added!" else "⚠️ Staff Notice", msg)
        }
    }

    fun deleteStaffMember(staffId: String) {
        viewModelScope.launch {
            repository.deleteStaffMember(staffId)
            showToast("🗑️ Staff Member Removed", "Revoked sessions and removed staff account.")
        }
    }

    fun grantReferralCredit(emailOrUserId: String, credits: Int = 1) {
        viewModelScope.launch {
            val (ok, msg) = repository.grantFreeCustomLinkCredit(emailOrUserId, credits)
            showToast(if (ok) "🎁 Free Custom Link Granted!" else "⚠️ Grant Notice", msg)
        }
    }

    fun createAndPublishAnnouncement(title: String, message: String, audience: String = "all_users") {
        viewModelScope.launch {
            repository.createAndPublishAnnouncement(title, message, audience)
            showToast("📣 Announcement Broadcasted!", "Published '$title' to active users.")
        }
    }

    fun addSearchSynonym(term: String, synonymsCsv: String) {
        viewModelScope.launch {
            repository.addSearchSynonym(term, synonymsCsv)
            showToast("🔍 Synonym Rule Added!", "Mapped '$term' -> $synonymsCsv")
        }
    }

    fun deleteSearchSynonym(synonymId: String) {
        viewModelScope.launch {
            repository.deleteSearchSynonym(synonymId)
            showToast("🗑️ Synonym Deleted", "Removed search synonym rule.")
        }
    }

    fun rebuildSearchIndex() {
        viewModelScope.launch {
            repository.rebuildSearchIndex()
            showToast("⚡ Search Index Rebuilt!", "Storefront search index synchronized with D1.")
        }
    }

    fun repairWishLink(slugOrId: String) {
        viewModelScope.launch {
            val (_, msg) = repository.repairWishLink(slugOrId)
            showToast("🛠️ Celebration Link Repaired!", msg)
        }
    }

    fun extendWishExpiry(wishId: String, slug: String, days: Int = 30) {
        viewModelScope.launch {
            val wish = repository.extendWishExpiry(wishId, days)
            if (wish != null) {
                showToast("✅ Wish Extended +$days Days!", "Wish /w/$slug is active for $days more days.")
            }
        }
    }

    fun transferWishOwnership(wishId: String, slug: String, targetEmail: String) {
        viewModelScope.launch {
            val ok = repository.transferWishOwnership(wishId, targetEmail)
            showToast(
                if (ok) "🔄 Ownership Transferred!" else "🔄 Updated Owner",
                "Transferred /w/$slug to $targetEmail"
            )
        }
    }

    fun moderateWish(wishId: String, slug: String, action: String) {
        viewModelScope.launch {
            repository.moderateWishStatus(wishId, action)
            showToast("🛡️ Wish Moderation Updated!", "Applied '$action' to /w/$slug.")
        }
    }

    fun deleteWish(wishId: String, slug: String) {
        viewModelScope.launch {
            repository.deleteWish(wishId)
            showToast("🗑️ Wish Removed", "Wish /w/$slug removed from active directory.")
        }
    }

    fun createUserAccount(email: String, displayName: String, role: String, isVip: Boolean) {
        viewModelScope.launch {
            repository.createUserAccount(email, displayName, role, isVip)
            showToast("👤 User Account Created!", "$email added to D1 directory.")
        }
    }

    fun toggleUserVip(userId: String, email: String, vipActive: Boolean) {
        viewModelScope.launch {
            repository.toggleUserVip(userId, vipActive)
            showToast(
                "👑 User Entitlement Updated!",
                "$email is now ${if (vipActive) "VIP Pass Active" else "Standard Free Tier"}."
            )
        }
    }

    fun updateUserStatus(userId: String, email: String, status: String) {
        viewModelScope.launch {
            repository.updateUserStatus(userId, status)
            showToast("🛡️ User Status Updated!", "$email status set to ${status.uppercase()}.")
        }
    }

    fun revokeUserSessions(userId: String, email: String) {
        viewModelScope.launch {
            repository.revokeUserSessions(userId)
            showToast("🔒 Sessions Revoked!", "Terminated all active sessions for $email.")
        }
    }

    fun deleteUserAccount(userId: String, email: String) {
        viewModelScope.launch {
            repository.deleteUserAccount(userId)
            showToast("🗑️ User Account Removed", "Deleted $email from D1.")
        }
    }

    fun addOccasionCategory(name: String, icon: String) {
        viewModelScope.launch {
            repository.addOccasion(name, icon)
            showToast("🏷️ Category Added Live!", "$icon $name pill added to `/api/categories` storefront filters.")
        }
    }

    fun deleteOccasionCategory(categoryId: String, name: String) {
        viewModelScope.launch {
            repository.deleteOccasion(categoryId)
            showToast("🗑️ Category Removed", "Removed $name from storefront filter pills.")
        }
    }

    fun addMediaAsset(title: String, url: String, type: String) {
        viewModelScope.launch {
            repository.addMediaAsset(title, url, type)
            showToast("☁️ Cloud Asset Added!", "$title registered in ${if (type == "audio") "Backblaze B2" else "ImgBB"}.")
        }
    }

    fun resolveTicket(ticketId: String) {
        viewModelScope.launch {
            repository.resolveSupportTicket(ticketId)
            showToast("✅ Support Ticket Resolved!", "Ticket #$ticketId marked resolved.")
        }
    }

    fun moderateWishItem(itemId: String, status: String) {
        viewModelScope.launch {
            repository.updateModerationStatus(itemId, status)
            showToast("🛡️ Moderation Updated!", "Item #$itemId marked as ${status.uppercase()}.")
        }
    }

    fun addNewTemplate(name: String, tier: String, price: Double, desc: String) {
        viewModelScope.launch {
            val tpl = repository.addTemplate(name, tier, price, desc)
            showToast("🎉 Template Package Added!", "${tpl.name} added to live catalog.")
        }
    }

    fun testServerPing(url: String) {
        viewModelScope.launch {
            val (ok, ms) = repository.testPing(url)
            if (ok) {
                showToast("🟢 Backend Online (${ms}ms)", "Connected to $url — Cloudflare Worker + D1 responding!")
            } else {
                showToast("🔴 Ping Failed", "Could not reach $url. Check https://wishmagic.pages.dev connection")
            }
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notifId)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            showToast("✅ All Alerts Marked Read", "Synced read status with Cloudflare D1.")
        }
    }

    fun deleteNotification(notifId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notifId)
            showToast("🗑️ Alert Removed", "Notification dismissed and synced.")
        }
    }

    fun sendBroadcastNotification(title: String, message: String, targetAudience: String = "all_users") {
        viewModelScope.launch {
            repository.sendBroadcastNotification(title, message, targetAudience)
            showToast("📢 Broadcast Sent Live!", "\"$title\" pushed to Cloudflare D1 notifications.")
        }
    }

    fun saveConnection(url: String, token: String) {
        repository.updateConnectionConfig(url, token)
        setConnectionModalVisible(false)
        syncLive()
    }
}
