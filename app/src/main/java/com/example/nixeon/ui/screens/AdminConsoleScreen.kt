package com.example.nixeon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.data.model.*
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.*
import com.example.nixeon.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsoleScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onBack: () -> Unit,
    onServerClick: (NixeonServer) -> Unit
) {
    if (!currentAccount.isOfficialAdmin) {
        LiquidBackground {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "AKSES DITOLAK: Anda tidak memiliki scope platform_admin atau akun resmi adm_01/adm_02.",
                    color = NixeonCrimson,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        return
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Users, 1: Live Servers, 2: Top-Ups, 3: Audit, 4: Migration & Backup

    val accounts by repository.accounts.collectAsState()
    val balances by repository.balances.collectAsState()
    val servers by repository.servers.collectAsState()
    val topUps by repository.topUps.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val vpsMigration by repository.vpsMigration.collectAsState()
    val plans by repository.plans.collectAsState()

    // Create User Dialog State
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newPanelRole by remember { mutableStateOf(PanelRole.USER) }
    var newPlanId by remember { mutableStateOf(plans.firstOrNull()?.id ?: "plan_growth") }
    var newGrantAmount by remember { mutableStateOf("50000") }
    var generatedActivationToken by remember { mutableStateOf<String?>(null) }

    // Reject TopUp Dialog State
    var topUpToReject by remember { mutableStateOf<TopUpTransaction?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    // Balance Adjustment Dialog State
    var userToAdjustBalance by remember { mutableStateOf<UserAccount?>(null) }
    var balanceAdjustAmount by remember { mutableStateOf("") }
    var balanceAdjustReason by remember { mutableStateOf("") }

    // Feedback message
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Nixeon Admin Console",
                                fontWeight = FontWeight.Bold,
                                color = NixeonAmber
                            )
                            Text(
                                "Aktor: ${currentAccount.adminSlot?.displayName} (${currentAccount.name})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NixeonTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Admin Mode Active Banner
                AdminModeBanner(adminSlotName = currentAccount.adminSlot?.displayName ?: "Official Admin")

                if (feedbackMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isFeedbackError) NixeonCrimson.copy(alpha = 0.25f) else NixeonEmerald.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isFeedbackError) NixeonCrimson else NixeonEmerald)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = feedbackMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = NixeonTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = NixeonTextPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Tab Row for Admin Submodules
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = NixeonAmber,
                    edgePadding = 0.dp,
                    divider = { Divider(color = NixeonGlassBorder) }
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Users (${accounts.size})") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Live Servers (${servers.size})") })
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            val pendingCount = topUps.count { it.status == TopUpStatus.PENDING }
                            Text(if (pendingCount > 0) "Top-Up ($pendingCount Pending)" else "Top-Up")
                        }
                    )
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Audit Log (${auditLogs.size})") })
                    Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Backup & Migrasi") })
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> {
                        // Users Tab
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MANAJEMEN AKUN USER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonTextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                )

                                Button(
                                    onClick = { showCreateUserDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NixeonAmber,
                                        contentColor = NixeonObsidian
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buat Akun User", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(accounts.values.toList()) { user ->
                                    val userBal = balances[user.id]?.balance ?: 0L
                                    LiquidGlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = 12.dp
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
                                                        text = user.name,
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = NixeonTextPrimary
                                                        )
                                                    )
                                                    if (user.isOfficialAdmin) {
                                                        LiquidBadge(text = user.adminSlot?.displayName ?: "Admin", badgeColor = NixeonAmber)
                                                    } else if (user.panelRole == PanelRole.ADMIN) {
                                                        LiquidBadge(text = "Panel Role: Admin", badgeColor = NixeonViolet)
                                                    }
                                                }
                                                Text(
                                                    text = "${user.email} • ID: ${user.id}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary, fontSize = 11.sp)
                                                )
                                                Text(
                                                    text = "Saldo: Rp${String.format(Locale.GERMAN, "%,d", userBal)}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = NixeonCyan, fontWeight = FontWeight.Bold)
                                                )
                                            }

                                            // Action to Adjust Balance
                                            IconButton(
                                                onClick = {
                                                    userToAdjustBalance = user
                                                    balanceAdjustAmount = ""
                                                    balanceAdjustReason = ""
                                                }
                                            ) {
                                                Icon(Icons.Default.PriceChange, contentDescription = "Sesuaikan Saldo", tint = NixeonEmerald)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Live Servers (Platform Admin All-Access)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Text(
                                    text = "SEMUA SERVER LIVE DI PANEL PTERODACTYL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonTextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            items(servers) { s ->
                                LiquidGlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = 12.dp,
                                    onClick = { onServerClick(s) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(s.name, fontWeight = FontWeight.Bold, color = NixeonTextPrimary)
                                            Text("Pemilik Akun: ${s.accountId} • Node #${s.nodeId}", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary, fontSize = 11.sp))
                                            Text("Alokasi: ${s.allocationIp}:${s.allocationPort} • ${s.eggName}", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextMuted, fontSize = 10.5.sp))
                                        }
                                        LiquidBadge(
                                            text = s.status.label,
                                            badgeColor = when (s.status) {
                                                ServerStatus.RUNNING -> NixeonEmerald
                                                ServerStatus.STOPPED -> Color.White.copy(alpha = 0.2f)
                                                else -> NixeonAmber
                                            },
                                            textColor = if (s.status == ServerStatus.STOPPED) NixeonTextPrimary else NixeonObsidian
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Top-Up Review Tab
                        val pendingList = topUps.filter { it.status == TopUpStatus.PENDING }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Text(
                                    text = "VERIFIKASI & APPROVAL TOP-UP (${pendingList.size} PENDING)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonTextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            if (pendingList.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                        Text("Tidak ada antrean top-up yang menunggu persetujuan.", color = NixeonTextMuted)
                                    }
                                }
                            } else {
                                items(pendingList) { tx ->
                                    LiquidGlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = 14.dp
                                    ) {
                                        Text(
                                            text = "Rp${String.format(Locale.GERMAN, "%,d", tx.amount)} • ${tx.method.label}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = NixeonCyan
                                            )
                                        )
                                        Text(
                                            text = "User ID: ${tx.accountId} • Pengirim: ${tx.senderName}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextPrimary)
                                        )
                                        Text(
                                            text = "Ref/RRN: ${tx.reference} • Lampiran: ${tx.proofFileId ?: "Tidak ada"}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary, fontSize = 11.5.sp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val res = repository.approveTopUp(currentAccount, tx.id)
                                                    res.fold(
                                                        onSuccess = {
                                                            feedbackMessage = "Top-Up ${tx.id} berhasil disetujui! Saldo ditambahkan ke ledger user."
                                                            isFeedbackError = false
                                                        },
                                                        onFailure = { ex ->
                                                            feedbackMessage = ex.message ?: "Approval gagal."
                                                            isFeedbackError = true
                                                        }
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NixeonEmerald, contentColor = NixeonObsidian),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Setujui (Approve)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    topUpToReject = tx
                                                    rejectionReason = ""
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NixeonCrimson, contentColor = Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Tolak", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Audit Log Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Text(
                                    text = "AUDIT TRAIL OPERASIONAL NIXEON",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonTextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            items(auditLogs) { log ->
                                val dateStr = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                LiquidGlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = 10.dp
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(log.action, fontWeight = FontWeight.Bold, color = NixeonAmber, fontSize = 12.sp)
                                        Text(dateStr, color = NixeonTextMuted, fontSize = 10.sp)
                                    }
                                    Text("Aktor: ${log.actorId} • Target: ${log.targetId}", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary, fontSize = 11.sp))
                                    Text("Alasan/Detail: ${log.reason}", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextPrimary, fontSize = 11.sp))
                                    Text("Hasil: ${log.result}", style = MaterialTheme.typography.bodySmall.copy(color = NixeonEmerald, fontSize = 10.5.sp))
                                }
                            }
                        }
                    }

                    4 -> {
                        // Backup & VPS Migration Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // Backup & Encrypted Snapshot Card
                            item {
                                LiquidGlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = 16.dp
                                ) {
                                    Text(
                                        text = "ARKIP CADANGAN ENKRIPSI (.tar.gz + AES-256)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NixeonCyan,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sesuai standar Nixeon: Snapshot .tar.gz harus dienkripsi dengan SHA-256 Checksum, Schema v7, serta dipisahkan antara Business DB, Panel DB, dan Server Volumes.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            val result = repository.generateBackupArchive(currentAccount)
                                            result.fold(
                                                onSuccess = { manifest ->
                                                    feedbackMessage = "Backup dibuat: ${manifest.archiveName}\nChecksum: ${manifest.checksumSha256.take(16)}... (Enkripsi: ${manifest.encryptionMethod})"
                                                    isFeedbackError = false
                                                },
                                                onFailure = { error ->
                                                    feedbackMessage = error.message ?: "Gagal membuat backup."
                                                    isFeedbackError = true
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NixeonCyan, contentColor = NixeonObsidian),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Buat Cadangan Terenkripsi Sekarang", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // VPS Cutover Stepper Card
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
                                        Text(
                                            text = "MIGRASI VPS INFRASTRUKTUR",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NixeonAmber,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        LiquidBadge(
                                            text = vpsMigration.currentPhase.name,
                                            badgeColor = NixeonAmber
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text("Fase Saat Ini: ${vpsMigration.currentPhase.label}", fontWeight = FontWeight.Bold, color = NixeonTextPrimary)
                                    Text("Target VPS Host: ${vpsMigration.targetHost}", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary))
                                    Text("Mode Pemeliharaan (Maintenance): ${if (vpsMigration.isMaintenanceMode) "AKTIF (Order & Top-Up Ditahan)" else "NONAKTIF"}", style = MaterialTheme.typography.bodySmall.copy(color = if (vpsMigration.isMaintenanceMode) NixeonCrimson else NixeonEmerald, fontWeight = FontWeight.Bold))

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                repository.updateVpsMigrationStep(currentAccount, VpsMigrationPhase.MAINTENANCE_CUTOVER, toggleMaintenance = true)
                                                feedbackMessage = "Maintenance Mode diaktifkan untuk persiapan Cutover VPS!"
                                                isFeedbackError = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NixeonAmber, contentColor = NixeonObsidian),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Set Maintenance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                repository.updateVpsMigrationStep(currentAccount, VpsMigrationPhase.COMPLETED, toggleMaintenance = false)
                                                feedbackMessage = "Migrasi VPS selesai! Token diputar dan DNS diperbarui."
                                                isFeedbackError = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NixeonEmerald, contentColor = NixeonObsidian),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Selesaikan Cutover", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create User Dialog (Official Admin Only)
        if (showCreateUserDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCreateUserDialog = false
                    generatedActivationToken = null
                },
                title = { Text("Buat Akun User Baru", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (generatedActivationToken != null) {
                            Text("AKUN BERHASIL DIBUAT!", fontWeight = FontWeight.Bold, color = NixeonEmerald)
                            Text("Kirimkan kode aktivasi satu-kali pakai berikut kepada user:", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = generatedActivationToken ?: "",
                                    modifier = Modifier.padding(12.dp),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = NixeonCyan
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = newEmail,
                                onValueChange = { newEmail = it },
                                label = { Text("Email Calon User") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Nama Lengkap") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newGrantAmount,
                                onValueChange = { newGrantAmount = it },
                                label = { Text("Saldo Awal Hibah / Grant (Rp)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pterodactyl Role Admin?", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary))
                                Switch(
                                    checked = newPanelRole == PanelRole.ADMIN,
                                    onCheckedChange = { isAdm -> newPanelRole = if (isAdm) PanelRole.ADMIN else PanelRole.USER }
                                )
                            }
                            Text(
                                text = "Catatan: Memberi Panel Role Admin tidak otomatis memberi hak Nixeon Admin Console.",
                                style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextMuted, fontSize = 11.sp)
                            )
                        }
                    }
                },
                confirmButton = {
                    if (generatedActivationToken == null) {
                        Button(
                            onClick = {
                                if (newEmail.isNotBlank() && newName.isNotBlank()) {
                                    val grant = newGrantAmount.toLongOrNull() ?: 0L
                                    val res = repository.adminCreateUser(
                                        adminActor = currentAccount,
                                        email = newEmail.trim(),
                                        name = newName.trim(),
                                        panelRole = newPanelRole,
                                        planId = newPlanId,
                                        initialGrantAmount = grant,
                                        allowedEggIds = listOf(1, 2, 3, 4, 5),
                                        activeDays = 30,
                                        gracePeriodDays = 7
                                    )
                                    res.fold(
                                        onSuccess = { (acc, token) ->
                                            generatedActivationToken = token
                                            feedbackMessage = "Akun ${acc.email} siap diaktifkan. Token: $token"
                                        },
                                        onFailure = { ex ->
                                            feedbackMessage = ex.message ?: "Gagal membuat user."
                                            isFeedbackError = true
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NixeonAmber, contentColor = NixeonObsidian)
                        ) {
                            Text("Generate Akun & Token", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                showCreateUserDialog = false
                                generatedActivationToken = null
                            }
                        ) {
                            Text("Selesai")
                        }
                    }
                },
                dismissButton = {
                    if (generatedActivationToken == null) {
                        TextButton(onClick = { showCreateUserDialog = false }) {
                            Text("Batal")
                        }
                    }
                },
                containerColor = NixeonSurface
            )
        }

        // Balance Adjustment Dialog (Reversal Ledger)
        if (userToAdjustBalance != null) {
            AlertDialog(
                onDismissRequest = { userToAdjustBalance = null },
                title = { Text("Koreksi / Adjust Saldo Ledger", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("User Target: ${userToAdjustBalance?.name} (${userToAdjustBalance?.id})")
                        Text("Peraturan: Saldo tidak boleh diedit bebas. Sistem akan membuat entri ledger transaksi koreksi append-only dengan alasan wajib.", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary))

                        OutlinedTextField(
                            value = balanceAdjustAmount,
                            onValueChange = { balanceAdjustAmount = it },
                            label = { Text("Jumlah Delta Saldo (Rp, contoh: 25000 atau -10000)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = balanceAdjustReason,
                            onValueChange = { balanceAdjustReason = it },
                            label = { Text("Alasan Penyesuaian (Wajib Audit)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = balanceAdjustAmount.toLongOrNull()
                            if (amt != null && balanceAdjustReason.isNotBlank()) {
                                userToAdjustBalance?.let { u ->
                                    repository.adminAdjustBalance(currentAccount, u.id, amt, balanceAdjustReason)
                                    feedbackMessage = "Saldo user ${u.name} berhasil disesuaikan Rp${amt}."
                                    isFeedbackError = false
                                }
                                userToAdjustBalance = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NixeonAmber, contentColor = NixeonObsidian)
                    ) {
                        Text("Simpan Transaksi Ledger", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToAdjustBalance = null }) {
                        Text("Batal")
                    }
                },
                containerColor = NixeonSurface
            )
        }

        // Reject TopUp Reason Dialog
        if (topUpToReject != null) {
            AlertDialog(
                onDismissRequest = { topUpToReject = null },
                title = { Text("Tolak Pengajuan Top-Up", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Masukkan alasan penolakan untuk transaksi ${topUpToReject?.id}:", style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary))
                        OutlinedTextField(
                            value = rejectionReason,
                            onValueChange = { rejectionReason = it },
                            label = { Text("Alasan Penolakan (Misal: Bukti tidak valid / Saldo belum masuk)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rejectionReason.isNotBlank()) {
                                topUpToReject?.let { tx ->
                                    repository.rejectTopUp(currentAccount, tx.id, rejectionReason)
                                    feedbackMessage = "Pengajuan ${tx.id} telah ditolak."
                                    isFeedbackError = false
                                }
                                topUpToReject = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NixeonCrimson, contentColor = Color.White)
                    ) {
                        Text("Tolak Transaksi", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { topUpToReject = null }) {
                        Text("Batal")
                    }
                },
                containerColor = NixeonSurface
            )
        }
    }
}
