package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminSection
import com.example.ui.AdminViewModel
import com.example.ui.ViewMode
import com.example.ui.clay.ClayButton
import com.example.ui.clay.ClayButtonVariant
import com.example.ui.clay.ClayColors
import com.example.ui.clay.clay3DBevel
import com.example.ui.components.BackendConnectionModal
import com.example.ui.components.ClayAdminSidebar
import com.example.ui.components.FirebaseGoogleWebAuthModal
import com.example.ui.components.LiveToast
import com.example.ui.components.TopHeaderBar
import com.example.ui.components.ZipImportModal
import com.example.ui.screens.*
import com.example.ui.theme.WishMagicClayTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initContext(applicationContext)
        handleZipIntent(intent)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()

            WishMagicClayTheme(darkTheme = isDark) {
                AdminAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleZipIntent(intent)
    }

    private fun handleZipIntent(intent: Intent?) {
        if (intent == null) return
        val uri: Uri? = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    ?: intent.clipData?.getItemAt(0)?.uri
                    ?: intent.data
            }
            else -> intent.data
        }
        if (uri != null) {
            viewModel.processIncomingZipUri(applicationContext, uri)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncLive(silent = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppContent(viewModel: AdminViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val section by viewModel.selectedSection.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val pingMs by viewModel.pingMs.collectAsState()
    val adminUser by viewModel.adminUser.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgressText by viewModel.syncProgressText.collectAsState()
    val showConnModal by viewModel.showConnectionModal.collectAsState()
    val showFirebaseWebAuthModal by viewModel.showFirebaseWebAuthModal.collectAsState()
    val showZipModal by viewModel.showZipImportDialog.collectAsState()
    val pendingZip by viewModel.pendingZipPackage.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val bearerToken by viewModel.bearerToken.collectAsState()

    val templates by viewModel.templates.collectAsState()
    val wishes by viewModel.wishes.collectAsState()
    val users by viewModel.users.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val activeFestival by viewModel.activeFestivalSpotlight.collectAsState()

    val toastVisible by viewModel.isToastVisible.collectAsState()
    val toastTitle by viewModel.toastTitle.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    var showAllModulesSheet by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isWideTabletOrDesktop = configuration.screenWidthDp >= 840

    BackHandler(
        enabled = drawerState.isOpen || viewMode == ViewMode.STUDIO_DOCK || section != AdminSection.OVERVIEW
    ) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            viewMode == ViewMode.STUDIO_DOCK -> viewModel.setViewMode(ViewMode.CLAY_NATIVE)
            section != AdminSection.OVERVIEW -> viewModel.selectSection(AdminSection.OVERVIEW)
        }
    }

    val openLiveSite = {
        val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim()
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(base)))
        }
        Unit
    }

    val bgBrush = if (!isDark) {
        Brush.verticalGradient(listOf(ClayColors.LightBg, ClayColors.LightBgEnd))
    } else {
        Brush.verticalGradient(listOf(ClayColors.DarkBg, ClayColors.DarkBgEnd))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = viewMode == ViewMode.CLAY_NATIVE && !isWideTabletOrDesktop,
        drawerContent = {
            ClayAdminSidebar(
                selectedSection = section,
                isDark = isDark,
                templatesCount = templates.size,
                wishesCount = wishes.size,
                usersCount = users.size,
                ordersCount = orders.size,
                hasActiveFestival = activeFestival != null,
                onSelectSection = {
                    viewModel.selectSection(it)
                    scope.launch { drawerState.close() }
                },
                onOpenLiveWebsite = openLiveSite,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (viewMode == ViewMode.CLAY_NATIVE && !isWideTabletOrDesktop) {
                    BottomClayDock(
                        currentSection = section,
                        isDark = isDark,
                        unreadNotificationsCount = notifications.count { !it.isRead },
                        onSectionSelect = { viewModel.selectSection(it) },
                        onMoreClick = { showAllModulesSheet = true },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush)
                    .padding(innerPadding)
            ) {
                if (isWideTabletOrDesktop) {
                    ClayAdminSidebar(
                        selectedSection = section,
                        isDark = isDark,
                        templatesCount = templates.size,
                        wishesCount = wishes.size,
                        usersCount = users.size,
                        ordersCount = orders.size,
                        hasActiveFestival = activeFestival != null,
                        onSelectSection = { viewModel.selectSection(it) },
                        onOpenLiveWebsite = openLiveSite,
                        onCloseDrawer = null
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Full-page content container: uses 100% of screen height with zero top white strip
                    val screenTopPadding = Modifier.fillMaxSize()

                    if (viewMode == ViewMode.STUDIO_DOCK) {
                        FullStudioDockScreen(viewModel = viewModel, modifier = screenTopPadding)
                    } else {
                        when (section) {
                            AdminSection.OVERVIEW -> DashboardScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.TEMPLATES -> TemplatesScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.ADD_TEMPLATE -> AddTemplateScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.FESTIVALS -> FestivalsScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.OCCASIONS -> OccasionsScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.MEDIA -> MediaLibraryScreen(viewModel = viewModel, onlyAudio = false, modifier = screenTopPadding)
                            AdminSection.MUSIC -> MediaLibraryScreen(viewModel = viewModel, onlyAudio = true, modifier = screenTopPadding)
                            AdminSection.WISHES -> WishesScreen(viewModel = viewModel, onlyExpiring = false, modifier = screenTopPadding)
                            AdminSection.EXPIRING -> WishesScreen(viewModel = viewModel, onlyExpiring = true, modifier = screenTopPadding)
                            AdminSection.ANALYTICS -> AnalyticsScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.USERS -> UsersScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.PRICING -> PricingScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.ORDERS -> OrdersScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.SETTINGS -> SettingsScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.STORAGE_HEALTH -> StorageHealthScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.BACKUPS -> StorageHealthScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.AUDIT_LOGS -> AuditLogsScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.SUPPORT -> SupportTicketsScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.MODERATION -> ModerationQueueScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.REFERRALS -> ReferralsStudioScreen(viewModel = viewModel, modifier = screenTopPadding)
                            AdminSection.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel, modifier = screenTopPadding)
                            else -> SpecializedStudioBridgeScreen(viewModel = viewModel, section = section, modifier = screenTopPadding)
                        }
                    }

                    // Independent Floating Top Header Buttons Overlaid on Top of the Page Content
                    TopHeaderBar(
                        viewMode = viewMode,
                        onViewModeChange = { viewModel.setViewMode(it) },
                        isDarkTheme = isDark,
                        onToggleTheme = { viewModel.toggleTheme() },
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        isConnected = isConnected,
                        pingMs = pingMs,
                        adminUser = adminUser,
                        isSyncing = isSyncing,
                        notifications = notifications,
                        onSyncClick = { viewModel.syncLive() },
                        onConnectionClick = {
                            viewModel.triggerInstantPing()
                            viewModel.setConnectionModalVisible(true)
                        },
                        onOpenDrawerClick = {
                            scope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                    // Subtle Progressive Streaming Status Pill between the two corner buttons
                    androidx.compose.animation.AnimatedVisibility(
                        visible = syncProgressText.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(999.dp))
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (!isDark) Color.White.copy(alpha = 0.94f) else Color(0xFF1E163B).copy(alpha = 0.94f))
                                .border(1.dp, ClayColors.Primary.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = syncProgressText,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ClayColors.Primary,
                                maxLines = 1
                            )
                        }
                    }

                    LiveToast(
                        visible = toastVisible,
                        title = toastTitle,
                        message = toastMsg,
                        isDarkTheme = isDark,
                        onDismiss = { viewModel.dismissToast() },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 52.dp)
                    )

                    if (showConnModal) {
                        BackendConnectionModal(
                            serverUrl = serverUrl,
                            bearerToken = bearerToken,
                            adminUser = adminUser,
                            isDarkTheme = isDark,
                            onDismiss = { viewModel.setConnectionModalVisible(false) },
                            onSave = { url, token ->
                                viewModel.saveConnection(url, token)
                            },
                            onSignInAdmin = { url, email, pass, token ->
                                viewModel.signInAndConnect(url, email, pass, token)
                            },
                            onSignInWithGoogle = {
                                viewModel.signInWithGoogle(context, openWebAuthIfBlocked = false)
                            },
                            onOpenFirebaseWebLogin = {
                                viewModel.setFirebaseWebAuthModalVisible(true)
                            },
                            onDirectMasterAdminSync = {
                                viewModel.signInWithDirectMasterAdmin()
                            },
                            onTestPing = { url ->
                                viewModel.testServerPing(url)
                            }
                        )
                    }

                    if (showFirebaseWebAuthModal) {
                        FirebaseGoogleWebAuthModal(
                            serverUrl = serverUrl,
                            isDarkTheme = isDark,
                            onAuthSuccess = { email, displayName, avatarUrl, uid, idToken, wmSessionCookie ->
                                viewModel.onFirebaseWebAuthSuccess(
                                    email = email,
                                    displayName = displayName,
                                    avatarUrl = avatarUrl,
                                    firebaseUid = uid,
                                    idToken = idToken,
                                    wmSessionCookie = wmSessionCookie
                                )
                            },
                            onDismiss = { viewModel.setFirebaseWebAuthModalVisible(false) }
                        )
                    }

                    if (showZipModal && pendingZip != null) {
                        ZipImportModal(
                            pendingZip = pendingZip!!,
                            isDarkTheme = isDark,
                            onPublishLive = { mode, name, price ->
                                viewModel.quickPublishPendingZip(mode, name, price)
                            },
                            onOpenInStudio = {
                                viewModel.dismissZipImportDialog()
                                viewModel.selectSection(AdminSection.ADD_TEMPLATE)
                            },
                            onDismiss = {
                                viewModel.dismissZipImportDialog()
                            }
                        )
                    }

                    if (showAllModulesSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showAllModulesSheet = false },
                            containerColor = if (!isDark) ClayColors.LightSurface else ClayColors.DarkSurface,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .navigationBarsPadding(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "🧭 All Website Admin Studio Modules",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (!isDark) ClayColors.LightText else ClayColors.DarkText
                                )

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 380.dp)
                                ) {
                                    items(AdminSection.values()) { item ->
                                        ClayButton(
                                            onClick = {
                                                viewModel.selectSection(item)
                                                showAllModulesSheet = false
                                            },
                                            variant = if (section == item) ClayButtonVariant.PRIMARY else ClayButtonVariant.SECONDARY,
                                            isDark = isDark,
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            Text(item.shortLabel, fontSize = 10.sp, maxLines = 1)
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
}

@Composable
fun BottomClayDock(
    currentSection: AdminSection,
    isDark: Boolean,
    unreadNotificationsCount: Int = 0,
    onSectionSelect: (AdminSection) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dockBrush = if (!isDark) {
        Brush.verticalGradient(listOf(Color.White, ClayColors.LightSurface, ClayColors.LightSurfaceEnd))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF2E2256), ClayColors.DarkSurface, ClayColors.DarkSurfaceEnd))
    }
    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val borderColor = if (!isDark) ClayColors.LightBorder else ClayColors.DarkBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, shape)
            .clip(shape)
            .background(dockBrush)
            .border(1.dp, borderColor, shape)
            .clay3DBevel(cornerRadius = 20.dp, isDark = isDark)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 46.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DockItem(
                label = "Dashboard",
                icon = Icons.Default.Home,
                isActive = currentSection == AdminSection.OVERVIEW,
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = { onSectionSelect(AdminSection.OVERVIEW) }
            )
            DockItem(
                label = "Templates",
                icon = Icons.Default.GridView,
                isActive = currentSection == AdminSection.TEMPLATES,
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = { onSectionSelect(AdminSection.TEMPLATES) }
            )
            Box(
                modifier = Modifier
                    .weight(1.15f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(11.dp), spotColor = ClayColors.Pink)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFF2A85), ClayColors.Pink, ClayColors.Primary)))
                        .clickable { onSectionSelect(AdminSection.FESTIVALS) }
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Festivals",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Festivals",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            DockItem(
                label = "Pricing",
                icon = Icons.Default.WorkspacePremium,
                isActive = currentSection == AdminSection.PRICING,
                isDark = isDark,
                modifier = Modifier.weight(1f),
                onClick = { onSectionSelect(AdminSection.PRICING) }
            )
            // Corner Notification Section in Bottom Bar
            DockItem(
                label = "Alerts",
                icon = Icons.Default.Notifications,
                isActive = currentSection == AdminSection.NOTIFICATIONS,
                isDark = isDark,
                badgeCount = unreadNotificationsCount,
                modifier = Modifier.weight(1f),
                onClick = { onSectionSelect(AdminSection.NOTIFICATIONS) }
            )
        }
    }
}

@Composable
fun DockItem(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    isDark: Boolean,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val activeColor = ClayColors.Pink
    val inactiveColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(21.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) activeColor else inactiveColor,
                    modifier = Modifier.size(18.dp)
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF43F5E))
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else "$badgeCount",
                            color = Color.White,
                            fontSize = 7.sp,
                            lineHeight = 7.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Text(
                text = label,
                color = if (isActive) activeColor else inactiveColor,
                fontSize = 8.5.sp,
                lineHeight = 10.sp,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
