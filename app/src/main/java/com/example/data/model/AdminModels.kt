package com.example.data.model

data class AdminUser(
    val id: String = "usr_fb_53BGpeJcywPta6qz",
    val email: String = "ankit.babusaini@gmail.com",
    val username: String = "anktech.ankit.babusaini",
    val displayName: String = "Ankit Babu Saini",
    val avatarUrl: String = "",
    val role: String = "OWNER",
    val isOwner: Boolean = true,
    val isAuthenticated: Boolean = false,
    val authMethod: String = "Firebase Google Auth (wish-magic-47944)"
)

data class TemplateFieldNode(
    val id: String,
    val label: String,
    val type: String = "text", // "text" | "textarea" | "image" | "images" | "audio" | "video" | "date" | "select" | "color" | "toggle"
    val section: String = "hero",
    val subsection: String = "",
    val defaultValue: String = "",
    val required: Boolean = false,
    val alias: String = "",
    val selector: String = "",
    val bindingTarget: String = "text",
    val placeholder: String = "",
    val maxLength: Int = 0
)

data class ZipFileEntryInfo(
    val path: String,
    val sizeKb: Double = 0.0,
    val category: String = "code", // "code" | "image" | "audio" | "video" | "manifest" | "ignored"
    val cloudTarget: String = "Backblaze B2" // "ImgBB CDN" | "Backblaze B2" | "D1 Manifest" | "Skipped (Dev File)"
)

data class TemplateSectionNode(
    val id: String,
    val label: String,
    val required: Boolean = false,
    val toggleable: Boolean = true,
    val enabled: Boolean = true,
    val order: Int = 1,
    val emptyParentPolicy: String = "collapse", // "collapse" | "keep"
    val fields: List<String> = emptyList(),
    val actions: List<String> = emptyList(),
    val photoSlotFieldId: String = "", // e.g. "photo_1" for data-wm-photo-slot="photo_1"
    val subsections: List<TemplateSectionNode> = emptyList()
)

data class TemplateActionNode(
    val id: String,
    val label: String,
    val type: String = "template_link", // "template_link" | "global_link" | "customizer" | "section" | "modal" | "interactive" | "internal_url" | "external_url" | "none"
    val logicalLink: String = "template.customize",
    val target: String = "/customize.html",
    val sectionId: String = "",
    val handler: String = "",
    val editableLabel: Boolean = true,
    val configurable: Boolean = true,
    val relationshipSpecific: Boolean = true,
    val required: Boolean = false,
    val enabled: Boolean = true,
    val fallbackBehavior: String = "hide" // "hide" | "disable" | "none"
)

data class TemplateVariantNode(
    val id: String,
    val slug: String,
    val name: String,
    val relationship: String,
    val relationshipLabel: String,
    val kind: String = "relationship", // "relationship" | "theme"
    val description: String = "",
    val entry: String = "index.html",
    val thumbnail: String = "",
    val status: String = "published", // "published" | "draft" | "disabled"
    val enabled: Boolean = true,
    val isDefault: Boolean = false,
    val order: Int = 1,
    val themePrimary: String = "#f59e0b",
    val themeSecondary: String = "#ec4899",
    val themeAccent: String = "#fde047",
    val themeBackground: String = "#160702",
    val themeText: String = "#ffffff",
    val cssVariables: Map<String, String> = emptyMap(),
    val messages: Map<String, String> = mapOf(
        "hero_title" to "SHUBH DEEPAVALI",
        "hero_subtitle" to "May thousands of lamps light up your life",
        "custom_message" to "Wishing you and your loved ones a radiant celebration filled with joy!",
        "secret_note" to "May this year bring every dream into reality!",
        "sender_name" to "— With Warm Regards"
    ),
    val visibilityOverrides: Map<String, Boolean> = emptyMap(),
    val actionOverrides: List<TemplateActionNode> = emptyList()
)

data class FestivalMetadataInfo(
    val festivalTags: List<String> = listOf("diwali", "deepavali", "bhai-dooj"),
    val suitableRelationships: List<String> = emptyList(),
    val campaignCompatibility: String = "evergreen_and_festival", // "festival_campaign" | "evergreen_and_festival" | "festival_only" | "any"
    val displayBadge: String = "✨ Special Edition",
    val suggestedDaysBefore: Int = 15,
    val suggestedDaysAfter: Int = 3,
    val advisoryOnly: Boolean = true
)

data class TemplateVersionItem(
    val id: String,
    val templateId: String,
    val versionNumber: Int = 1,
    val versionLabel: String = "1.4.0",
    val status: String = "published", // "published" | "draft" | "archived"
    val isCurrent: Boolean = true,
    val filesCount: Int = 5,
    val storageProvider: String = "Backblaze B2 + ImgBB",
    val releaseNotes: String = "UTP v1.4.0 immutable package release",
    val createdAt: String = "2026-10-22"
)

data class TemplateItem(
    val id: String,
    val slug: String,
    val name: String,
    val description: String,
    val tier: String = "free", // "free" | "vip" | "premium"
    val price: Double = 0.0,
    val status: String = "published", // "published" | "draft"
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false,
    val placementPriority: Int = 1,
    val thumbnail: String,
    val category: String = "Celebration",
    val variantsCount: Int = 0,
    val enabledVariants: List<String> = emptyList(),
    val schemaVersion: String = "1.4.0",
    val templateVersion: String = "1.4.0",
    val festivalBadge: String = "",
    val filesCount: Int = 5,
    val storageProvider: String = "Backblaze B2 / ImgBB",
    val previewUrl: String = "",
    val currentVersionId: String = "",
    val hasUnpublishedDraft: Boolean = false,
    val capabilities: List<String> = listOf(
        "photos", "multiplePhotos", "gallery", "music", "countdown",
        "themes", "animations", "particles", "sections", "subsections",
        "actions", "secretMessage", "variants", "festivalMetadata"
    ),
    val photoSlots: List<String> = listOf("photo_1", "photo_2"),
    val festivalMetadata: FestivalMetadataInfo = FestivalMetadataInfo(),
    val fields: List<TemplateFieldNode> = emptyList(),
    val sections: List<TemplateSectionNode> = listOf(
        TemplateSectionNode(
            id = "hero",
            label = "Hero Opening Section",
            required = true,
            toggleable = false,
            enabled = true,
            order = 1,
            emptyParentPolicy = "keep",
            fields = listOf("recipient_name", "hero_title", "hero_subtitle"),
            actions = listOf("hero_cta", "explore_memories_btn"),
            subsections = listOf(
                TemplateSectionNode("hero_blessing_badge", "Auspicious Blessing Crest", required = false, toggleable = true, enabled = true, order = 1),
                TemplateSectionNode("hero_cta_bar", "Hero Action Bar", required = false, toggleable = true, enabled = true, order = 2, actions = listOf("hero_cta", "explore_memories_btn"))
            )
        ),
        TemplateSectionNode(
            id = "countdown",
            label = "Celebration Countdown",
            required = false,
            toggleable = true,
            enabled = true,
            order = 2,
            fields = listOf("event_date")
        ),
        TemplateSectionNode(
            id = "message",
            label = "Heartfelt Tribute Letter",
            required = true,
            toggleable = false,
            enabled = true,
            order = 3,
            fields = listOf("custom_message", "sender_name")
        ),
        TemplateSectionNode(
            id = "gallery",
            label = "Photo Memories Gallery",
            required = false,
            toggleable = true,
            enabled = true,
            order = 4,
            emptyParentPolicy = "collapse",
            fields = listOf("photo_1", "photo_2"),
            subsections = listOf(
                TemplateSectionNode("gallery_photo_1", "Featured Photo Slot 1", required = false, toggleable = true, enabled = true, order = 1, fields = listOf("photo_1"), photoSlotFieldId = "photo_1"),
                TemplateSectionNode("gallery_photo_2", "Memory Photo Slot 2", required = false, toggleable = true, enabled = true, order = 2, fields = listOf("photo_2"), photoSlotFieldId = "photo_2")
            )
        ),
        TemplateSectionNode(
            id = "secret_message",
            label = "Secret Vault Blessing",
            required = false,
            toggleable = true,
            enabled = true,
            order = 5,
            fields = listOf("secret_note"),
            actions = listOf("explore_more_btn")
        )
    ),
    val actions: List<TemplateActionNode> = listOf(
        TemplateActionNode("hero_cta", "Make For Yours", "template_link", "template.customize", "/customize.html", "hero_cta_bar"),
        TemplateActionNode("explore_memories_btn", "View Festive Gallery", "section", "", "gallery", "hero_cta_bar"),
        TemplateActionNode("explore_more_btn", "Explore More", "global_link", "global.explore", "/templates.html", "secret_message", relationshipSpecific = false)
    ),
    val variantsList: List<TemplateVariantNode> = emptyList(),
    val versionsList: List<TemplateVersionItem> = emptyList()
)

data class FestivalItem(
    val id: String,
    val slug: String,
    val name: String,
    val effectiveDate: String,
    val daysBefore: Int = 15,
    val daysAfter: Int = 3,
    val badgeLabel: String,
    val isActive: Boolean = false,
    val hasManualOverride: Boolean = false,
    val assignedTemplatesCount: Int = 4,
    val allowedVariants: List<String> = listOf("family", "sister", "brother", "best_friend"),
    val campaignId: String = "",
    val campaignStatus: String = "ACTIVE", // "ACTIVE" | "SCHEDULED" | "PAUSED" | "ARCHIVED"
    val windowStartDate: String = "",
    val windowEndDate: String = "",
    val firstBlockedDate: String = "",
    val dateSource: String = "VERIFIED_PANCHANG_TABLE",
    val assignedTemplateIds: List<String> = emptyList()
)

data class WishItem(
    val id: String,
    val slug: String,
    val recipientName: String,
    val senderName: String,
    val templateId: String,
    val templateName: String = "Custom Celebration",
    val relationship: String = "family",
    val createdAt: String,
    val expiresAt: String,
    val status: String = "published", // "published" | "upcoming" | "expired" | "flagged" | "suspended"
    val lifecycleStatus: String = "active",
    val moderationStatus: String = "approved",
    val isFlagged: Boolean = false,
    val isLocked: Boolean = false,
    val viewsCount: Int = 0,
    val sharesCount: Int = 0,
    val thumbnail: String = ""
)

data class UserItem(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String = "creator", // "admin" | "vip" | "creator"
    val vipActive: Boolean = false,
    val status: String = "active", // "active" | "suspended" | "banned"
    val wishesCount: Int = 0,
    val referralsCount: Int = 0,
    val createdAt: String = "2026-10-15"
)

data class OrderItem(
    val id: String,
    val amount: Double,
    val currency: String = "INR",
    val status: String = "PAID", // "PAID" | "PENDING" | "REFUNDED"
    val customerEmail: String,
    val recipientName: String = "",
    val planName: String,
    val paymentGateway: String = "Razorpay",
    val wishSlug: String = "",
    val isComplimentary: Boolean = false,
    val createdAt: String = "2026-10-21"
)

data class CouponItem(
    val code: String,
    val discountType: String = "percentage", // "percentage" | "flat"
    val discountValue: Int = 20,
    val typeLabel: String = "20% OFF VIP Pass",
    val redemptions: String = "0 / 1,000",
    val maxRedemptions: Int = 1000,
    val expiry: String = "Dec 31, 2026",
    val status: String = "Active" // "Active" | "Disabled"
)

data class StaffMemberItem(
    val id: String,
    val userId: String = "",
    val name: String,
    val mobile: String,
    val email: String = "",
    val role: String = "ADMIN", // "OWNER" | "ADMIN" | "MANAGER" | "EDITOR" | "SUPPORT" | "ANALYST"
    val permissions: List<String> = listOf("*"),
    val status: String = "active",
    val createdAt: String = "2026-10-15"
)

data class AnnouncementItem(
    val id: String,
    val internalTitle: String,
    val title: String,
    val message: String,
    val targetAudience: String = "all_users",
    val status: String = "published", // "draft" | "published" | "archived"
    val createdAt: String = "2026-10-22"
)

data class SearchSynonymItem(
    val id: String,
    val term: String,
    val synonyms: List<String>,
    val boostCategory: String = ""
)

data class UserCreationItem(
    val id: String,
    val slug: String,
    val recipientName: String,
    val userName: String,
    val userEmail: String,
    val templateName: String,
    val photosCount: Int = 0,
    val photoUrls: List<String> = emptyList(),
    val hasVoiceNote: Boolean = false,
    val voiceNoteUrl: String = "",
    val hasVideo: Boolean = false,
    val videoUrl: String = "",
    val customMessage: String = "",
    val templateThumbnail: String = "",
    val status: String = "Active",
    val createdAt: String = "2026-10-22"
)

data class AdminNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: String = "system",
    val timeAgo: String = "Just now",
    val isRead: Boolean = false
)

data class OccasionItem(
    val id: String,
    val slug: String,
    val name: String,
    val icon: String,
    val count: Int,
    val isActive: Boolean = true
)

data class MediaAssetItem(
    val id: String,
    val title: String,
    val url: String,
    val rawUrl: String = "",
    val streamUrl: String = "",
    val thumbnailUrl: String = "",
    val type: String = "image", // "image" | "audio" | "video"
    val format: String = "PNG", // "MP3" | "WAV" | "PNG" | "JPG" | "MP4"
    val provider: String = "ImgBB", // "ImgBB" | "Backblaze B2" | "Static"
    val rawProvider: String = "imgbb", // "b2" | "imgbb" | "static" | "d1"
    val sizeKb: Int = 240,
    val sizeFormatted: String = "240 KB",
    val artist: String = "",
    val duration: String = "",
    val category: String = "Storefront Asset",
    val storageKey: String = "",
    val deleteUrl: String = "",
    val isTemplateImage: Boolean = false,
    val templateName: String = "",
    val isUntracked: Boolean = false,
    val isInUse: Boolean = false,
    val isDemo: Boolean = false,
    val uploaderRole: String = "admin", // "admin" | "user"
    val userId: String = "",
    val userName: String = "Admin Studio",
    val userEmail: String = "",
    val wishSlug: String = "",
    val recipientName: String = "",
    val createdAt: String = "2026-10-21"
)

data class MediaSummaryInfo(
    val totalAssets: Int = 0,
    val photosCount: Int = 0,
    val templateImagesCount: Int = 0,
    val videosCount: Int = 0,
    val audioCount: Int = 0,
    val b2CloudCount: Int = 0,
    val imgbbCount: Int = 0,
    val untrackedCloudCount: Int = 0,
    val adminUploadsCount: Int = 0,
    val userUploadsCount: Int = 0,
    val currentHeroImage: String = "/assets/file_ab2082.png"
)

data class AuditLogItem(
    val id: String,
    val action: String,
    val actorEmail: String = "ankit.babusaini@gmail.com",
    val target: String,
    val details: String,
    val createdAt: String
)

data class AnalyticsSummary(
    val totalRevenueInr: Int = 0,
    val paidOrdersCount: Int = 0,
    val totalWishesCreated: Int = 0,
    val activeWishesCount: Int = 0,
    val upcomingWishesCount: Int = 0,
    val expiredWishesCount: Int = 0,
    val totalViewsCount: Int = 0,
    val totalSharesCount: Int = 0,
    val totalFavoritesCount: Int = 0,
    val activeVipSubscribers: Int = 0,
    val conversionRatePercent: Double = 0.0,
    val topTemplateName: String = "Glitter Birthday Bash",
    val activeVisitorsNow: Int = 1,
    val categoryBreakdown: Map<String, Int> = emptyMap()
)

data class SupportTicketItem(
    val id: String,
    val subject: String,
    val customerEmail: String,
    val status: String = "open", // "open" | "resolved"
    val priority: String = "high",
    val message: String,
    val wishSlug: String = "",
    val createdAt: String = "2026-10-22"
)

data class ModerationItem(
    val id: String,
    val wishSlug: String,
    val reason: String,
    val status: String = "approved", // "pending" | "approved" | "rejected"
    val submittedBy: String,
    val createdAt: String = "2026-10-22"
)

data class SiteSettings(
    // Cat 1: General & Branding
    val siteName: String = "WishMagic",
    val siteTagline: String = "Create Magical Celebration Websites in 60 Seconds",
    val heroBadgeText: String = "Make Every Moment Magical ✨",
    val heroHeadline: String = "Gift a Magical Web-Experience!",
    val heroSubheadline: String = "Turn birthdays, anniversaries & festivals into interactive 3D celebration websites.",
    val supportEmail: String = "ankit.babusaini@gmail.com",
    val maintenanceMode: Boolean = false,
    val freeOfferModeEnabled: Boolean = false,
    // Cat 2: Hero 3D & 4-View Background + 1..50 Blur
    val heroDisplayMode: String = "diwali-3d", // "diwali-3d" | "image"
    val heroPhotoUrl: String = "/assets/file_ab2082.png",
    val heroBgTheme: String = "cosmic", // "clean" | "gradient" | "cosmic" | "midnight" | "romantic" | "sunset" | "aurora"
    val siteBackgroundMode: String = "diwali-palace", // "diwali-palace" | "cloudy"
    val siteBackgroundBlur: Int = 18, // 1 to 50
    // Cat 3: Central Link Manager (UTP v1.3.0 / v1.4.0)
    val canonicalBaseUrl: String = "https://wishmagic.pages.dev",
    val customDomainUrl: String = "https://wishmagic.pages.dev",
    val preserveTemplateContextOnLinks: Boolean = true,
    val globalRoutes: Map<String, String> = mapOf(
        "global.home" to "/",
        "global.explore" to "/templates.html",
        "global.browseTemplates" to "/templates.html",
        "global.marketplace" to "/marketplace.html",
        "global.createWish" to "/create.html",
        "global.pricing" to "/pricing.html",
        "global.help" to "/help.html",
        "global.contact" to "/contact.html"
    ),
    val templateActionPatterns: Map<String, String> = mapOf(
        "template.customize" to "/customize.html?template={templateId}&variant={variant}&rel={rel}",
        "template.create" to "/customize.html?template={templateId}&variant={variant}&rel={rel}",
        "template.preview" to "/template-preview.html?template={templateId}&variant={variant}&rel={rel}"
    ),
    // Cat 4: Wish Lifecycle & Expiry
    val defaultFreeExpiryDays: Int = 7,
    val defaultPaidExpiryDays: Int = 365,
    val defaultVipExpiryDays: Int = 730,
    val maskUpcomingWishesBeforeDate: Boolean = true,
    // Cat 5 & 6: Payments
    val razorpayEnabled: Boolean = true,
    val stripeEnabled: Boolean = true,
    val upiInstantEnabled: Boolean = true,
    // Cat 7: Cloud Storage
    val b2BucketName: String = "wishmagic-assets",
    val imgbbCdnEnabled: Boolean = true,
    // Cat 8: Announcements & Notifications
    val announcementBannerText: String = "✨ Special Diwali & Celebration Templates Live Now!",
    val announcementBannerEnabled: Boolean = true,
    val emailRemindersEnabled: Boolean = true,
    // Cat 9 & 10: Security & SEO
    val zeroTrustCsrfEnabled: Boolean = true,
    val seoDefaultTitle: String = "WishMagic — Personalized 3D Celebration & Festival Websites",
    val popularTags: List<String> = listOf("Diwali", "Birthday", "Anniversary", "Best Friend", "Sister", "Brother")
)

data class VipPlanItem(
    val id: String,
    val name: String,
    val badge: String = "👑 VIP PLAN",
    val price: Int = 49,
    val originalPrice: Int = 99,
    val billingLabel: String = "one-time",
    val description: String = "",
    val ctaText: String = "",
    val isPopular: Boolean = false,
    val isActive: Boolean = true,
    val freeCustomLink: Boolean = true,
    val freeHdPhotos: Boolean = true,
    val freeCustomStyle: Boolean = true,
    val unlockVipTemplates: Boolean = true,
    val customMusicVoice: Boolean = true,
    val removeWatermark: Boolean = true,
    val lifetimeLink: Boolean = true,
    val maxPhotos: Int = 16,
    val validityDays: Int = 0,
    val features: List<String> = emptyList()
)

fun defaultVipPlansList(): List<VipPlanItem> = listOf(
    VipPlanItem(
        id = "vip_pass",
        name = "VIP Magic Pass",
        badge = "👑 VIP PASS",
        price = 49,
        originalPrice = 99,
        billingLabel = "one-time",
        description = "Unlock VIP templates, custom link (/w/your-name), HD photos, custom styles, voice & music, zero watermark, and lifetime link.",
        ctaText = "Unlock VIP Magic Pass ✨",
        isPopular = true,
        isActive = true,
        freeCustomLink = true,
        freeHdPhotos = true,
        freeCustomStyle = true,
        unlockVipTemplates = true,
        customMusicVoice = true,
        removeWatermark = true,
        lifetimeLink = true,
        features = listOf(
            "Unlock VIP & Premium 3D Templates",
            "Free Custom Link (/w/your-name) Included",
            "Up to 16 Uncompressed HD Photos & Custom Styles",
            "Custom Music & 2-Min Voice Dedication",
            "100% Zero Watermark & Permanent Lifetime Link"
        )
    ),
    VipPlanItem(
        id = "personalized_link",
        name = "Custom Link Pack",
        badge = "ADD-ON PACK 1",
        price = 59,
        originalPrice = 79,
        billingLabel = "/ wish",
        description = "Replace random codes with your own custom celebration URL (/w/happy-birthday-priya).",
        ctaText = "Choose Custom Link",
        isPopular = false,
        isActive = true,
        freeCustomLink = true,
        freeHdPhotos = false,
        freeCustomStyle = false,
        unlockVipTemplates = false,
        customMusicVoice = false,
        removeWatermark = false,
        lifetimeLink = false,
        features = listOf(
            "Custom Personalized URL (/w/your-name)",
            "Instant Real-Time Availability Check",
            "Works on Any Free or Premium Template",
            "Free via 7-Friends Referral Reward"
        )
    ),
    VipPlanItem(
        id = "hd_image_style_pack",
        name = "HD Images + Custom Style",
        badge = "ADD-ON PACK 2",
        price = 59,
        originalPrice = 79,
        billingLabel = "/ wish",
        description = "Upload crystal-clear High Quality (HD) photos and customize fonts, colors, frames & particle styles.",
        ctaText = "Choose HD Images & Style",
        isPopular = false,
        isActive = true,
        freeCustomLink = false,
        freeHdPhotos = true,
        freeCustomStyle = true,
        unlockVipTemplates = false,
        customMusicVoice = true,
        removeWatermark = true,
        lifetimeLink = false,
        features = listOf(
            "Uncompressed High-Quality (HD) Photos (Up to 16)",
            "Custom Fonts, Color Themes & Frames",
            "Polaroid, Filmstrip & Mosaic Layouts",
            "Custom Particle & Celebration Effects"
        )
    ),
    VipPlanItem(
        id = "free_template_combo",
        name = "Custom Link + HD & Style Combo",
        badge = "🔥 ALL-IN-ONE COMBO",
        price = 99,
        originalPrice = 118,
        billingLabel = "/ wish",
        description = "For any Free Template: Get both Custom Link + High Quality Images + Custom Style together in one combo!",
        ctaText = "Unlock All-in-One Combo ✨",
        isPopular = true,
        isActive = true,
        freeCustomLink = true,
        freeHdPhotos = true,
        freeCustomStyle = true,
        unlockVipTemplates = false,
        customMusicVoice = true,
        removeWatermark = true,
        lifetimeLink = true,
        features = listOf(
            "Custom Link (/w/your-name) Included",
            "HD Quality Images (Up to 16) Included",
            "Full Custom Style, Fonts & Themes",
            "Zero Watermark & QR Share Card"
        )
    ),
    VipPlanItem(
        id = "premium_template_combo",
        name = "Premium Template + Full Combo",
        badge = "👑 ROYAL COMBO ADD-ON",
        price = 70,
        originalPrice = 118,
        billingLabel = "+ template price",
        description = "Picking a Premium Template? Add this combo to get the Template + Custom Link + HD Images + Custom Style!",
        ctaText = "Explore Premium Combo →",
        isPopular = false,
        isActive = true,
        freeCustomLink = true,
        freeHdPhotos = true,
        freeCustomStyle = true,
        unlockVipTemplates = true,
        customMusicVoice = true,
        removeWatermark = true,
        lifetimeLink = true,
        features = listOf(
            "Unlocks Selected Premium 3D Template",
            "Includes Custom Link + HD Photos + Style",
            "Custom Music, Voice Note & Zero Watermark",
            "Permanent Lifetime Celebration Link"
        )
    )
)

data class PricingConfig(
    val customSlugPrice: Int = 59,
    val hdMediaPrice: Int = 59,
    val freeTemplateComboPrice: Int = 99,
    val premiumComboAddonPrice: Int = 70,
    val vipPassMonthlyPrice: Int = 49,
    val referralRequiredCount: Int = 7,
    val vipPlans: List<VipPlanItem> = defaultVipPlansList()
)

data class StorageHealthInfo(
    val d1Status: String = "HEALTHY",
    val d1LatencyMs: Long = 18L,
    val b2Status: String = "CONNECTED",
    val b2AssetCount: Int = 0,
    val imgbbStatus: String = "ONLINE",
    val imgbbAssetCount: Int = 0,
    val cdnLatencyMs: Long = 24L,
    val orphanAssetsCount: Int = 0,
    val quarantineCount: Int = 0
)

enum class AdminNavGroup(val label: String) {
    MAIN("MAIN"),
    CONTENT("TEMPLATES"),
    WISHES("WISHES"),
    USERS("USERS"),
    SYSTEM("SYSTEM"),
    MORE_STUDIO("MORE STUDIO SECTIONS")
}

enum class AdminSection(
    val key: String,
    val shortLabel: String,
    val title: String,
    val pill: String,
    val desc: String,
    val group: AdminNavGroup,
    val badgeDefault: String = ""
) {
    // MAIN
    OVERVIEW(
        "overview",
        "Dashboard",
        "WishMagic Dashboard",
        "Welcome Back, Admin 🪄",
        "Manage templates, users, wishes and make every celebration magical!",
        AdminNavGroup.MAIN,
        "LIVE"
    ),

    // TEMPLATES
    TEMPLATES(
        "templates",
        "All Templates",
        "Templates Library & Upload Studio",
        "UTP v1.4.0",
        "Manage live templates, edit capabilities, toggle section visibility, and control relationship variants.",
        AdminNavGroup.CONTENT
    ),
    ADD_TEMPLATE(
        "add-template",
        "Add Template",
        "Add & Upload New Template Studio",
        "2-OPTION SMART ZIP",
        "Upload Full Template ZIP or Changed-Files-Only Diff Patch directly to Backblaze B2, ImgBB & D1.",
        AdminNavGroup.CONTENT,
        "v1.4"
    ),
    FESTIVALS(
        "festivals",
        "Festival Scheduler",
        "Festival Scheduler & Main Website Campaign Studio",
        "F - 15 TO F + 3 WINDOW",
        "Launch festival campaigns in 1-Click with live countdown banner, filter pills & variant assignments.",
        AdminNavGroup.CONTENT,
        "🪔"
    ),
    OCCASIONS(
        "occasions",
        "Categories",
        "Occasions & Categories Manager",
        "STOREFRONT TAXONOMY",
        "Manage Main Website occasion filter pills (#occasion-pills) and celebration categories.",
        AdminNavGroup.CONTENT
    ),
    MEDIA(
        "media",
        "Media Library",
        "Media Assets, Hero Photo & Vault",
        "ADMIN & USER MEDIA",
        "Separate Admin & User uploads with Photo, Audio, Video & User-level filters.",
        AdminNavGroup.CONTENT
    ),
    MUSIC(
        "music",
        "Music Library",
        "Celebration Music & Audio Synth Library",
        "B2 AUDIO STREAM",
        "Manage background music tracks and celebration audio streams.",
        AdminNavGroup.CONTENT
    ),

    // WISHES
    WISHES(
        "wishes",
        "All Wishes",
        "Wishes & Celebrations Registry",
        "LIVE /w/:slug",
        "Inspect live customer wishes, preview /w/:slug, extend +7d/+30d/VIP, moderate, or transfer.",
        AdminNavGroup.WISHES
    ),
    EXPIRING(
        "expiring",
        "Expiring Soon",
        "Expiring Soon Wishes",
        "RETENTION ALERT",
        "Extend expiring customer celebration links with 1-Click +7 Days or +30 Days retention.",
        AdminNavGroup.WISHES
    ),
    ANALYTICS(
        "analytics",
        "Analytics",
        "Analytics & Growth Insights",
        "REAL-TIME TELEMETRY",
        "Revenue trajectory, creator growth, publication funnel, and template performance.",
        AdminNavGroup.WISHES
    ),

    // USERS
    USERS(
        "users",
        "All Users",
        "Users & Creators Directory",
        "RBAC & VIP PASS",
        "Create user accounts, grant VIP Pass, update roles, or manage account status.",
        AdminNavGroup.USERS
    ),
    PRICING(
        "pricing",
        "Subscriptions",
        "Subscriptions, Plans & Coupons",
        "LIVE CHECKOUT ENGINE",
        "Create, edit, or delete VIP plans (including ₹49 VIP Pass), free perks & promo codes.",
        AdminNavGroup.USERS
    ),
    ORDERS(
        "orders",
        "Payments",
        "Payments & VIP Orders Registry",
        "RAZORPAY & COMP PASS",
        "Track settled payments and issue Complimentary VIP Gift Passes with instant /w/vip-... links.",
        AdminNavGroup.USERS
    ),

    // SYSTEM
    SETTINGS(
        "settings",
        "Settings",
        "Settings & Storefront CMS",
        "Main Website Live CMS ✨",
        "Configure live main website hero text & visuals, 50-level blur, central links, and all 12 platform categories.",
        AdminNavGroup.SYSTEM,
        "CMS"
    ),
    STORAGE_HEALTH(
        "storage-health",
        "Storage",
        "Storage Health & Backups",
        "D1 + B2 + IMGBB",
        "Monitor Cloudflare D1, Backblaze B2 & ImgBB health, verify integrity, and clean up orphans.",
        AdminNavGroup.SYSTEM
    ),
    BACKUPS(
        "backups",
        "Backups",
        "Database Backups & Snapshot Export",
        "SAFE SNAPSHOTS",
        "Create and export full JSON snapshots of site settings, templates, and D1 configurations.",
        AdminNavGroup.SYSTEM
    ),
    AUDIT_LOGS(
        "audit-logs",
        "Activity Logs",
        "Activity & Audit Logs",
        "IMMUTABLE TRAIL",
        "Chronological security and administrative audit trail across all modules.",
        AdminNavGroup.SYSTEM
    ),

    // MORE STUDIO SECTIONS
    USER_CREATIONS(
        "user-creations",
        "User Creations",
        "User Uploads & Wish Creations",
        "CUSTOM MEDIA VAULT",
        "Inspect user-uploaded photos, voice notes, custom videos, and personalized wish submissions.",
        AdminNavGroup.MORE_STUDIO
    ),
    SEARCH(
        "search",
        "Search & Discovery",
        "Search & Discovery Diagnostics",
        "SYNONYMS & INDEX",
        "Manage search synonyms, query expansion rules, and rebuild storefront search index.",
        AdminNavGroup.MORE_STUDIO
    ),
    REFERRALS(
        "referrals",
        "Referrals & Rewards",
        "Referral Program & Rewards",
        "7-FRIENDS MILESTONE",
        "Share with 7 Friends → Unlock 1 Free Custom Link (Worth ₹59). Grant credits & review attributions.",
        AdminNavGroup.MORE_STUDIO
    ),
    MODERATION(
        "moderation",
        "Moderation Queue",
        "Content Moderation & Flagged Queue",
        "TRUST & SAFETY",
        "Review flagged wishes, user appeals, and enforce account safety restrictions.",
        AdminNavGroup.MORE_STUDIO
    ),
    SUPPORT(
        "support",
        "Support Console",
        "Support Console & Link Auto-Repair",
        "DIAGNOSE & REPAIR",
        "Lookup entities, run celebration link diagnostics, 1-click auto-repair (+7d), and resolve tickets.",
        AdminNavGroup.MORE_STUDIO
    ),
    STAFF(
        "staff",
        "Staff & Team",
        "Staff & Access Control (RBAC)",
        "OWNER & STAFF",
        "Add staff members, assign roles (ADMIN, MANAGER, EDITOR, SUPPORT, ANALYST), and manage access.",
        AdminNavGroup.MORE_STUDIO
    ),
    LEGAL(
        "legal",
        "Legal & Compliance",
        "Legal Policies & Compliance",
        "PRIVACY & TERMS",
        "Manage Privacy Policy, Terms of Service, Refund Policy, and user acceptance audit records.",
        AdminNavGroup.MORE_STUDIO
    ),
    ANNOUNCEMENTS(
        "announcements",
        "Announcements",
        "Platform Announcements Console",
        "GLOBAL BROADCAST",
        "Create and publish global announcements to active users and storefront banners.",
        AdminNavGroup.MORE_STUDIO
    ),
    NOTIFICATIONS(
        "notifications",
        "Alerts & Queue",
        "Notifications & Delivery Operations",
        "LIVE ALERTS",
        "View live platform notifications, wish & payment alerts, and notification delivery queue.",
        AdminNavGroup.MORE_STUDIO,
        "BELL"
    )
}

data class PendingZipPackage(
    val fileName: String,
    val bytes: ByteArray,
    val sizeKb: Int,
    val suggestedName: String,
    val detectedId: String = "",
    val detectedSlug: String = "",
    val detectedDescription: String = "",
    val detectedCategory: String = "festival",
    val detectedTier: String = "free",
    val detectedPrice: Double = 0.0,
    val detectedSchemaVersion: String = "1.4.0",
    val detectedTemplateVersion: String = "1.4.0",
    val detectedVariants: List<String> = emptyList(),
    val detectedSectionsCount: Int = 5,
    val detectedFieldsCount: Int = 8,
    val hasValidManifest: Boolean = false,
    val matchedExistingTemplateId: String = "",
    val matchedExistingTemplateName: String = "",
    val extractedCoverUri: String = "",
    val stagedPreviewFilePath: String = "",
    val stagedPreviewBaseUrl: String = "",
    val stagedPreviewHtml: String = "",
    val fields: List<TemplateFieldNode> = emptyList(),
    val capabilities: List<String> = emptyList(),
    val fileEntries: List<ZipFileEntryInfo> = emptyList(),
    val imagesCount: Int = 0,
    val audioCount: Int = 0,
    val videoCount: Int = 0,
    val codeFilesCount: Int = 0,
    val ignoredDevFilesCount: Int = 0,
    val sections: List<TemplateSectionNode> = emptyList(),
    val actions: List<TemplateActionNode> = emptyList(),
    val variantsList: List<TemplateVariantNode> = emptyList(),
    val festivalMetadata: FestivalMetadataInfo = FestivalMetadataInfo(),
    val photoSlots: List<String> = listOf("photo_1", "photo_2"),
    val validationPassed: Boolean = true,
    val validationChecks: List<Pair<String, Boolean>> = emptyList(),
    val validationErrors: List<String> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingZipPackage) return false
        return fileName == other.fileName && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
