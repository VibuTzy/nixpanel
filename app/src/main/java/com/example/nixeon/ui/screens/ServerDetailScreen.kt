package com.example.nixeon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.data.model.*
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.*
import com.example.nixeon.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    serverId: String,
    onBack: () -> Unit
) {
    val servers by repository.servers.collectAsState()
    val server = servers.find { it.id == serverId }

    var selectedTab by remember { mutableStateOf(0) } // 0: Console, 1: Files, 2: Config
    val consoleListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var consoleInput by remember { mutableStateOf("") }
    var fileList by remember { mutableStateOf<List<ServerFileItem>>(emptyList()) }
    var selectedFileToEdit by remember { mutableStateOf<ServerFileItem?>(null) }
    var fileEditorContent by remember { mutableStateOf("") }
    var fileToDelete by remember { mutableStateOf<ServerFileItem?>(null) }

    var showKillConfirmation by remember { mutableStateOf(false) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    // Fetch initial files
    LaunchedEffect(serverId) {
        fileList = repository.getServerFiles(serverId)
    }

    if (server == null) {
        LiquidBackground {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Server tidak ditemukan atau telah dihapus.", color = NixeonTextSecondary)
            }
        }
        return
    }

    val isOwner = server.accountId == currentAccount.id
    val isAdminDelegated = !isOwner && currentAccount.isOfficialAdmin

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                server.name,
                                fontWeight = FontWeight.Bold,
                                color = NixeonTextPrimary,
                                maxLines = 1
                            )
                            Text(
                                "${server.eggName} • ${server.allocationIp}:${server.allocationPort}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NixeonTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = NixeonTextPrimary
                            )
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
                // Admin Delegated Access Banner
                if (isAdminDelegated) {
                    AdminModeBanner(
                        adminSlotName = "Akses Delegasi: Akun Pemilik ${server.accountId}"
                    )
                }

                // Server Status & Live Power Actions
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STATUS KONEKSI WINGS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NixeonTextSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            LiquidBadge(
                                text = server.status.label,
                                badgeColor = when (server.status) {
                                    ServerStatus.RUNNING -> NixeonEmerald
                                    ServerStatus.STOPPED, ServerStatus.OFFLINE -> Color.White.copy(alpha = 0.25f)
                                    ServerStatus.SUSPENDED -> NixeonCrimson
                                    else -> NixeonAmber
                                },
                                textColor = if (server.status == ServerStatus.STOPPED) NixeonTextPrimary else NixeonObsidian
                            )
                        }

                        // Power Action Buttons Row
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Start
                            IconButton(
                                onClick = {
                                    repository.performServerPowerAction(currentAccount, server.id, "start")
                                    actionFeedback = "Sinyal START dikirim ke Wings Node."
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NixeonEmerald.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = NixeonEmerald)
                            }

                            // Restart
                            IconButton(
                                onClick = {
                                    repository.performServerPowerAction(currentAccount, server.id, "restart")
                                    actionFeedback = "Sinyal RESTART dikirim ke Wings Node."
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NixeonCyan.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = NixeonCyan)
                            }

                            // Stop
                            IconButton(
                                onClick = {
                                    repository.performServerPowerAction(currentAccount, server.id, "stop")
                                    actionFeedback = "Sinyal STOP dikirim ke Wings Node."
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NixeonAmber.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = NixeonAmber)
                            }

                            // Kill (Destructive)
                            IconButton(
                                onClick = { showKillConfirmation = true },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NixeonCrimson.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Dangerous, contentDescription = "Kill", tint = NixeonCrimson)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricSmallBox(label = "RAM", value = "${server.ramUsageMb.toInt()} / ${server.memoryMb} MB", modifier = Modifier.weight(1f))
                        MetricSmallBox(label = "CPU", value = "${String.format(Locale.US, "%.1f", server.cpuUsagePercent)}%", modifier = Modifier.weight(1f))
                        MetricSmallBox(label = "Disk", value = "${server.diskUsageMb.toInt()} / ${server.diskMb} MB", modifier = Modifier.weight(1f))
                    }

                    if (actionFeedback != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = actionFeedback ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = NixeonCyan, fontSize = 11.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs (Console, Files, Settings)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = NixeonCyan,
                    divider = { Divider(color = NixeonGlassBorder) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Live Console", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            fileList = repository.getServerFiles(server.id)
                        },
                        text = { Text("File Manager", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Alokasi & Info", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> {
                        // Interactive Live Terminal Console
                        val logs = repository.getConsoleLogs(server.id)
                        LaunchedEffect(logs.size) {
                            if (logs.isNotEmpty()) {
                                consoleListState.animateScrollToItem(logs.size - 1)
                            }
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Terminal output box
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF030712),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NixeonGlassBorder)
                            ) {
                                LazyColumn(
                                    state = consoleListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                ) {
                                    items(logs) { logLine ->
                                        val line = logLine.text
                                        Text(
                                            text = line,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = when {
                                                    line.contains("WARN") -> NixeonAmber
                                                    line.contains("ERROR") || line.contains("Exception") -> NixeonCrimson
                                                    line.contains("Done") || line.contains("ready") -> NixeonEmerald
                                                    line.contains(">") -> NixeonCyan
                                                    else -> Color(0xFF9CA3AF)
                                                }
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Command input row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = consoleInput,
                                    onValueChange = { consoleInput = it },
                                    placeholder = { Text("Ketik perintah (contoh: help, say Halo)...", color = NixeonTextMuted, fontSize = 12.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            if (consoleInput.isNotBlank()) {
                                                repository.sendConsoleCommand(currentAccount, server.id, consoleInput.trim())
                                                consoleInput = ""
                                            }
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("console_command_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = NixeonTextPrimary,
                                        unfocusedTextColor = NixeonTextPrimary,
                                        focusedBorderColor = NixeonCyan,
                                        unfocusedBorderColor = NixeonGlassBorder,
                                        focusedContainerColor = Color(0xFF030712),
                                        unfocusedContainerColor = Color(0xFF030712)
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (consoleInput.isNotBlank()) {
                                            repository.sendConsoleCommand(currentAccount, server.id, consoleInput.trim())
                                            consoleInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NixeonCyan)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Kirim", tint = NixeonObsidian)
                                }
                            }
                        }
                    }

                    1 -> {
                        // File Manager Tab
                        if (selectedFileToEdit != null) {
                            // File Editor View
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mengedit: ${selectedFileToEdit?.path}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = NixeonCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = { selectedFileToEdit = null }) {
                                            Text("Tutup", color = NixeonTextSecondary)
                                        }
                                        Button(
                                            onClick = {
                                                selectedFileToEdit?.let { f ->
                                                    repository.saveServerFile(currentAccount, server.id, f.path, fileEditorContent)
                                                    fileList = repository.getServerFiles(server.id)
                                                    selectedFileToEdit = null
                                                    actionFeedback = "File ${f.path} berhasil disimpan."
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NixeonCyan, contentColor = NixeonObsidian),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Simpan", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = fileEditorContent,
                                    onValueChange = { fileEditorContent = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = NixeonTextPrimary,
                                        unfocusedTextColor = NixeonTextPrimary,
                                        focusedBorderColor = NixeonCyan,
                                        unfocusedBorderColor = NixeonGlassBorder,
                                        focusedContainerColor = Color(0xFF030712),
                                        unfocusedContainerColor = Color(0xFF030712)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        } else {
                            // File List View
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(fileList) { file ->
                                    LiquidGlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = 12.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                                    contentDescription = null,
                                                    tint = if (file.isDirectory) NixeonAmber else NixeonCyan,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = file.name,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = NixeonTextPrimary
                                                        )
                                                    )
                                                    Text(
                                                        text = if (file.isDirectory) "Direktori" else "${file.sizeBytes} bytes",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = NixeonTextMuted,
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (!file.isDirectory) {
                                                    IconButton(
                                                        onClick = {
                                                            selectedFileToEdit = file
                                                            fileEditorContent = file.contentPreview ?: ""
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit File", tint = NixeonCyan)
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { fileToDelete = file }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Hapus File", tint = NixeonCrimson)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Allocation & Node Info Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                LiquidGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                                    Text("INFORMASI INFRASTRUKTUR WINGS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = NixeonTextSecondary))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    SummaryRow(label = "Server UUID", value = server.id)
                                    SummaryRow(label = "Node", value = "Node ID #${server.nodeId} (Singapore Datacenter)")
                                    SummaryRow(label = "Alokasi IP", value = server.allocationIp)
                                    SummaryRow(label = "Alokasi Port", value = server.allocationPort.toString())
                                    SummaryRow(label = "Egg Software", value = server.eggName)
                                    SummaryRow(label = "Masa Aktif", value = "Aktif (Auto-renewal enabled)")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Kill Confirmation Dialog
        ConfirmationDialog(
            show = showKillConfirmation,
            title = "Paksa Berhenti (KILL SERVER)?",
            message = "Perintah Kill akan menghentikan proses server seketika tanpa menyimpan chunk atau state game terakhir. Tindakan ini dicatat ke audit log.",
            confirmLabel = "KILL SEKARANG",
            isDestructive = true,
            onConfirm = {
                repository.performServerPowerAction(currentAccount, server.id, "kill")
                actionFeedback = "Sinyal KILL SERVER telah dieksekusi."
            },
            onDismiss = { showKillConfirmation = false }
        )

        // Delete File Confirmation Dialog
        ConfirmationDialog(
            show = fileToDelete != null,
            title = "Hapus File Server?",
            message = "Apakah Anda yakin ingin menghapus '${fileToDelete?.path}'? File yang terhapus tidak dapat dipulihkan.",
            confirmLabel = "HAPUS PERMANEN",
            isDestructive = true,
            onConfirm = {
                fileToDelete?.let { f ->
                    repository.deleteServerFile(currentAccount, server.id, f.path)
                    fileList = repository.getServerFiles(server.id)
                    actionFeedback = "File ${f.path} berhasil dihapus."
                    fileToDelete = null
                }
            },
            onDismiss = { fileToDelete = null }
        )
    }
}

@Composable
private fun MetricSmallBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = NixeonTextMuted))
            Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NixeonTextPrimary))
        }
    }
}
