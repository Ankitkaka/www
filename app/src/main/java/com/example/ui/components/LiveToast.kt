package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.clay.ClayColors
import com.example.ui.clay.clay3DBevel

@Composable
fun LiveToast(
    visible: Boolean,
    title: String,
    message: String,
    isDarkTheme: Boolean,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        val shape = RoundedCornerShape(18.dp)
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .shadow(12.dp, shape, spotColor = ClayColors.Emerald.copy(alpha = 0.7f))
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            ClayColors.EmeraldLight,
                            ClayColors.Emerald,
                            ClayColors.EmeraldDark
                        )
                    )
                )
                .clay3DBevel(cornerRadius = 18.dp, isDark = false, tintBevelColor = Color(0x66000000))
                .border(1.2.dp, Color.White.copy(alpha = 0.6f), shape)
                .clickable { onDismiss?.invoke() }
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = title.ifBlank { "✅ Ho Gaya! Live Website Updated" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = message.ifBlank { "Changes synced to Cloudflare D1 & storefront." },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 2
                    )
                }
                if (onDismiss != null) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
