package com.example.nixeon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.data.model.*
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.*
import com.example.nixeon.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserHomeScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onNavigateToServers: () -> Unit,
    onNavigateToCreateServer: () -> Unit,
    onNavigateToTopUp: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAdminConsole: () -> Unit,
    onServerClick: (NixeonServer) -> Unit,
    onLogout: () -> Unit
) {
    val balances by repository.balances.collectAsState()
    val servers by repository.servers.collectAsState()
    val entitlements by repository.entitlements.collectAsState()
    val plans by repository.plans.collectAsState()
    val notifications by repository.notifications.collectAsState()

    val userBalance = balances[currentAccount.id]
    val userServers = servers.filter { it.accountId == currentAccount.id }
    val userEntitlement = entitlements[currentAccount.id]
    val currentPlan = plans.find { it.id == userEntitlement?.planId } ?: plans.firstOrNull()
    val unreadNotifs = notifications.count { it.accountId == currentAccount.id && !it.isRead }

    val formattedBalance = remember(userBalance?.balance) {
        val bal = userBalance?.balance ?: 0L
        String.format(Locale.GERMAN, "Rp%,d", bal)
    }

    val expiryDateStr = remember(userEntitlement?.expiresAt) {
        val exp = userEntitlement?.expiresAt
        if (exp != null) {
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(exp))
        } else "-"
    }

    LiquidBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Header: Profile, Role Badge, Notification bell, Logout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Halo, ${currentAccount.name}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NixeonTextPrimary
                                )
                            )
                            if (currentAccount.panelRole == PanelRole.ADMIN) {
                                LiquidBadge(
                                    text = "Panel Admin",
                                    badgeColor = NixeonViolet,
                                    textColor = NixeonObsidian
                                )
                            }
                        }

                        Text(
                            text = if (currentAccount.isOfficialAdmin) currentAccount.adminSlot?.displayName ?: "Official Admin" else "ID Akun: ${currentAccount.id}",
                            style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifs > 0) {
                                        Badge(containerColor = NixeonCrimson) {
                                            Text(unreadNotifs.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = NixeonCyan
                                )
                            }
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Keluar",
                                tint = NixeonTextSecondary
                            )
                        }
                    }
                }
            }

            // If official admin, display Admin Mode banner & shortcut
            if (currentAccount.isOfficialAdmin) {
                item {
                    AdminModeBanner(adminSlotName = currentAccount.adminSlot?.displayName ?: "Admin")
                    LiquidOutlinedButton(
                        text = "Buka Nixeon Admin Console",
                        icon = Icons.Default.Security,
                        onClick = onNavigateToAdminConsole,
                        borderColor = NixeonAmber,
                        textColor = NixeonAmber,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Balance Card
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SALDO AKTIF",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NixeonTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        SyncTimestampBadge(timestamp = userBalance?.lastSyncedAt ?: System.currentTimeMillis())
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formattedBalance,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = NixeonTextPrimary
                        )
                    )

                    if ((userBalance?.holdBalance ?: 0L) > 0L) {
                        Text(
                            text = "Hold Saldo Provisioning: Rp${String.format(Locale.GERMAN, "%,d", userBalance?.holdBalance)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NixeonAmber,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidPrimaryButton(
                            text = "Top-Up Saldo",
                            icon = Icons.Default.AddCard,
                            onClick = onNavigateToTopUp,
                            modifier = Modifier.weight(1f),
                            testTag = "home_topup_button"
                        )

                        LiquidOutlinedButton(
                            text = "Buat Server",
                            icon = Icons.Default.Add,
                            onClick = onNavigateToCreateServer,
                            modifier = Modifier.weight(1f),
                            borderColor = NixeonCyan,
                            textColor = NixeonCyan,
                            testTag = "home_create_server_button"
                        )
                    }
                }
            }

            // Resource Entitlement & Plan Card
            item {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PAKET & ENTITLEMENT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NixeonTextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = currentPlan?.name ?: "Starter Node",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NixeonCyan
                                )
                            )
                        }

                        LiquidBadge(
                            text = userEntitlement?.status?.label ?: "Aktif",
                            badgeColor = if (userEntitlement?.status == EntitlementStatus.GRACE_PERIOD) NixeonAmber else NixeonEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resource Meters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val maxRamMb = currentPlan?.memoryMb ?: 2048
                        val usedRamMb = userServers.sumOf { it.ramUsageMb.toInt() }
                        val usedDiskMb = userServers.sumOf { it.diskUsageMb.toInt() }
                        val maxDiskMb = currentPlan?.diskMb ?: 25600

                        MetricMiniCard(
                            label = "Server",
                            value = "${userServers.size}/${currentPlan?.maxServers ?: 1}",
                            modifier = Modifier.weight(1f),
                            color = NixeonCyan
                        )
                        MetricMiniCard(
                            label = "RAM Alokasi",
                            value = "${maxRamMb / 1024} GB",
                            modifier = Modifier.weight(1f),
                            color = NixeonEmerald
                        )
                        MetricMiniCard(
                            label = "Disk Ruang",
                            value = "${maxDiskMb / 1024} GB",
                            modifier = Modifier.weight(1f),
                            color = NixeonViolet
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Masa Aktif s/d: $expiryDateStr",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NixeonTextMuted,
                                fontSize = 11.5.sp
                            )
                        )
                        Text(
                            text = "Detail Paket",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NixeonCyan,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { onNavigateToPlans() }
                        )
                    }
                }
            }

            // Quick Actions Grid
            item {
                Text(
                    text = "MENU UTAMA",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NixeonTextSecondary,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Redeem Voucher",
                        icon = Icons.Default.CardGiftcard,
                        color = NixeonEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRewards
                    )
                    QuickActionTile(
                        title = "Bonus & Bansos",
                        icon = Icons.Default.VolunteerActivism,
                        color = NixeonAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRewards
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = "Riwayat Ledger",
                        icon = Icons.Default.ReceiptLong,
                        color = NixeonViolet,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                    QuickActionTile(
                        title = "Bantuan Support",
                        icon = Icons.Default.SupportAgent,
                        color = NixeonCyan,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSupport
                    )
                }
            }

            // Active Servers Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SERVER SAYA (${userServers.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NixeonCyan,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { onNavigateToServers() }
                    )
                }
            }

            if (userServers.isEmpty()) {
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 24.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = NixeonTextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Belum Ada Server Aktif",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NixeonTextPrimary
                                )
                            )
                            Text(
                                text = "Deploy game server Minecraft, Node.js bot, atau script Anda sekarang.",
                                style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            LiquidPrimaryButton(
                                text = "Buat Server Pertama",
                                onClick = onNavigateToCreateServer,
                                modifier = Modifier.widthIn(max = 220.dp)
                            )
                        }
                    }
                }
            } else {
                items(userServers) { server ->
                    ServerListItemCard(
                        server = server,
                        onClick = { onServerClick(server) }
                    )
                }
            }
        }
    }
}

@Composable
fun ServerListItemCard(
    server: NixeonServer,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 14.dp,
        onClick = onClick,
        testTag = "server_item_${server.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextPrimary
                        )
                    )
                }

                Text(
                    text = "${server.eggName} • ${server.allocationIp}:${server.allocationPort}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NixeonTextSecondary,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RAM: ${server.ramUsageMb.toInt()} / ${server.memoryMb} MB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NixeonTextMuted,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "CPU: ${String.format(Locale.US, "%.1f", server.cpuUsagePercent)}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NixeonTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                LiquidBadge(
                    text = server.status.label,
                    badgeColor = when (server.status) {
                        ServerStatus.RUNNING -> NixeonEmerald
                        ServerStatus.STOPPED, ServerStatus.OFFLINE -> Color.White.copy(alpha = 0.2f)
                        ServerStatus.SUSPENDED -> NixeonCrimson
                        else -> NixeonAmber
                    },
                    textColor = if (server.status == ServerStatus.STOPPED) NixeonTextPrimary else NixeonObsidian
                )
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Detail",
                    tint = NixeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = NixeonTextMuted
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = NixeonTextPrimary
                )
            )
        }
    }
}
