package com.example.nixeon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.data.model.UserAccount
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.LiquidBackground
import com.example.nixeon.ui.components.LiquidGlassCard
import com.example.nixeon.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onBack: () -> Unit
) {
    val notifications by repository.notifications.collectAsState()
    val userNotifications = notifications.filter { it.accountId == currentAccount.id }

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pemberitahuan", fontWeight = FontWeight.Bold, color = NixeonTextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = NixeonTextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (userNotifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada pemberitahuan baru.", color = NixeonTextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(userNotifications) { notif ->
                        val dateStr = remember(notif.createdAt) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(notif.createdAt))
                        }

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 14.dp,
                            onClick = { repository.markNotificationAsRead(notif.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Normal,
                                        color = if (!notif.isRead) NixeonCyan else NixeonTextPrimary
                                    )
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NixeonTextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}
