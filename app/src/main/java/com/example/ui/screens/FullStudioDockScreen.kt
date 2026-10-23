package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.AdminViewModel
import com.example.ui.ViewMode
import com.example.ui.clay.ClayButton
import com.example.ui.clay.ClayButtonVariant
import com.example.ui.clay.ClayCard
import com.example.ui.clay.ClayColors
import com.example.ui.clay.CompactIconButton

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FullStudioDockScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkTheme.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val bearerToken by viewModel.bearerToken.collectAsState()
    val section by viewModel.selectedSection.collectAsState()
    val context = LocalContext.current

    val fullUrl = remember(serverUrl, section) {
        val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
        "$base/admin/anktech/anktech/admin/${section.key}"
    }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val textColor = if (!isDark) ClayColors.LightText else ClayColors.DarkText
    val mutedColor = if (!isDark) ClayColors.LightTextMuted else ClayColors.DarkTextMuted

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClayCard(
            isDark = isDark,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isLoading) ClayColors.Amber else ClayColors.Emerald)
                    )
                    Column {
                        Text(
                            text = "Full Studio Dock: ${section.shortLabel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = fullUrl,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = mutedColor,
                            maxLines = 1
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CompactIconButton(
                        onClick = { webViewInstance?.reload() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = ClayColors.Primary, modifier = Modifier.size(16.dp))
                    }

                    CompactIconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in Browser", tint = ClayColors.Primary, modifier = Modifier.size(16.dp))
                    }

                    ClayButton(
                        onClick = { viewModel.setViewMode(ViewMode.CLAY_NATIVE) },
                        variant = ClayButtonVariant.PRIMARY,
                        isDark = isDark,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Clay View", fontSize = 10.sp)
                    }
                }
            }
        }

        ClayCard(
            isDark = isDark,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp)),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.setSupportZoom(true)
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false

                            runCatching {
                                val cm = CookieManager.getInstance()
                                cm.setAcceptCookie(true)
                                cm.setAcceptThirdPartyCookies(this, true)
                                if (bearerToken.isNotBlank()) {
                                    val base = serverUrl.ifBlank { "https://wishmagic.pages.dev" }.trim().removeSuffix("/")
                                    cm.setCookie(base, "wm_session=$bearerToken; Path=/; Secure; SameSite=Lax")
                                    cm.flush()
                                }
                            }

                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    val themeMode = if (isDark) "dark" else "light"
                                    val js = """
                                        (function(){
                                          try {
                                            document.documentElement.setAttribute('data-theme', '$themeMode');
                                            localStorage.setItem('wm_admin_theme', '$themeMode');
                                            var st = document.getElementById('wm-clay-dock-inject');
                                            if (!st) {
                                              st = document.createElement('style');
                                              st.id = 'wm-clay-dock-inject';
                                              st.textContent = 'aside.admin-sidebar, #admin-sidebar { display: none !important; } main, .admin-main-content { margin-left: 0 !important; width: 100% !important; max-width: 100% !important; }';
                                              document.head.appendChild(st);
                                            }
                                          } catch(e){}
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(js, null)
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return false
                                }
                            }

                            loadUrl(fullUrl, mapOf("X-WishMagic-Admin-App" to "1"))
                            webViewInstance = this
                        }
                    },
                    update = { view ->
                        if (view.url != fullUrl) {
                            view.loadUrl(fullUrl, mapOf("X-WishMagic-Admin-App" to "1"))
                        }
                    }
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (!isDark) Color(0x88FFFFFF) else Color(0x88000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ClayColors.Primary)
                    }
                }
            }
        }
    }
}
