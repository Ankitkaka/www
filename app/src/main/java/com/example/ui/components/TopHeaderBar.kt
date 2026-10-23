package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminNotificationItem
import com.example.data.model.AdminUser
import com.example.ui.ViewMode

/**
 * Pure 2-Button Fixed Floating Corner Overlay (Zero Top Header Strip / Zero "White Patti"):
 * - Top-Left Corner: 3-Line Sidebar Drawer Menu Button (`☰`)
 * - Top-Right Corner: Single-Button Theme Toggle (`☀️` / `🌙` — switches between Light & Dark)
 * - No background bar, no search bar, no profile button, no notification button, no extra icons.
 */
@Composable
fun TopHeaderBar(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isConnected: Boolean,
    pingMs: Long,
    adminUser: AdminUser,
    isSyncing: Boolean,
    notifications: List<AdminNotificationItem> = emptyList(),
    onSyncClick: () -> Unit,
    onConnectionClick: () -> Unit,
    onOpenDrawerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val floatBg = if (!isDarkTheme) {
        Color(0xEBFFFFFF)
    } else {
        Color(0xEB221346)
    }
    val borderColor = if (!isDarkTheme) Color(0xFFD5C5F2) else Color(0xFF5B3EA8)
    val iconTint = if (!isDarkTheme) Color(0xFF2B194D) else Color(0xFFF5F0FF)
    val btnShape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // 1. TOP-LEFT FIXED FLOATING BUTTON: 3-Line Menu (☰)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(34.dp)
                .shadow(6.dp, btnShape, spotColor = Color(0x557C3AED))
                .clip(btnShape)
                .background(floatBg)
                .border(1.dp, borderColor, btnShape)
                .clickable(onClick = onOpenDrawerClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open Sidebar Menu",
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        // 2. TOP-RIGHT FIXED FLOATING BUTTON: Single Theme Toggle (☀️ / 🌙)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(34.dp)
                .shadow(6.dp, btnShape, spotColor = Color(0x66EC4899))
                .clip(btnShape)
                .background(
                    if (!isDarkTheme) {
                        Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)))
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899)))
                    }
                )
                .border(1.dp, Color(0x66FFFFFF), btnShape)
                .clickable(onClick = onToggleTheme),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (!isDarkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                contentDescription = if (!isDarkTheme) "Switch to Dark Theme" else "Switch to Light Theme",
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
