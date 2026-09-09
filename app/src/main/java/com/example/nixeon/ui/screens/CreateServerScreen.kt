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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.data.model.*
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.*
import com.example.nixeon.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServerScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onBack: () -> Unit,
    onServerCreated: (NixeonServer) -> Unit
) {
    val plans by repository.plans.collectAsState()
    val eggs by repository.eggs.collectAsState()
    val nodes by repository.nodes.collectAsState()
    val balances by repository.balances.collectAsState()
    val entitlements by repository.entitlements.collectAsState()

    val userBalance = balances[currentAccount.id]?.balance ?: 0L
    val userEntitlement = entitlements[currentAccount.id]

    var serverName by remember { mutableStateOf("Server ${currentAccount.name.take(6)}") }
    var selectedPlanId by remember { mutableStateOf(userEntitlement?.planId ?: plans.firstOrNull()?.id ?: "plan_growth") }
    var selectedEggId by remember { mutableStateOf(eggs.firstOrNull()?.id ?: 1) }
    var selectedNodeId by remember { mutableStateOf(nodes.firstOrNull()?.id ?: 1) }

    var isDeploying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedPlan = plans.find { it.id == selectedPlanId } ?: plans.firstOrNull()
    val selectedEgg = eggs.find { it.id == selectedEggId } ?: eggs.firstOrNull()
    val selectedNode = nodes.find { it.id == selectedNodeId } ?: nodes.firstOrNull()

    val canAfford = userBalance >= (selectedPlan?.pricePerMonth ?: 0L)

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Deploy Server Baru",
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextPrimary
                        )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // Info banner
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 12.dp,
                        backgroundColor = Color(0xFF13283F).copy(alpha = 0.5f),
                        borderColor = NixeonCyan.copy(alpha = 0.3f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = NixeonCyan, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Sistem menerapkan Hold-and-Commit: Saldo ditahan sementara, dan hanya dikurangi setelah alokasi Pterodactyl berhasil.",
                                style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextPrimary, fontSize = 11.5.sp)
                            )
                        }
                    }
                }

                // 1. Server Name Input
                item {
                    Text(
                        text = "1. NAMA SERVER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = serverName,
                        onValueChange = { serverName = it },
                        label = { Text("Nama Pengenal Server", color = NixeonTextSecondary) },
                        placeholder = { Text("Contoh: Survival Craft Nusantara", color = NixeonTextMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NixeonTextPrimary,
                            unfocusedTextColor = NixeonTextPrimary,
                            focusedBorderColor = NixeonCyan,
                            unfocusedBorderColor = NixeonGlassBorder,
                            focusedContainerColor = Color.White.copy(alpha = 0.04f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        )
                    )
                }

                // 2. Select Software / Game Egg
                item {
                    Text(
                        text = "2. PILIH SOFTWARE / GAME ENGINE (EGG)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        eggs.forEach { egg ->
                            EggSelectorTile(
                                egg = egg,
                                isSelected = egg.id == selectedEggId,
                                onClick = { selectedEggId = egg.id }
                            )
                        }
                    }
                }

                // 3. Select Node Location
                item {
                    Text(
                        text = "3. LOKASI DATACENTER (NODE)",
                        style = MaterialTheme.typography.labelSmall.copy(
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
                        nodes.forEach { node ->
                            NodeSelectorTile(
                                node = node,
                                isSelected = node.id == selectedNodeId,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedNodeId = node.id }
                            )
                        }
                    }
                }

                // 4. Select Plan Package
                item {
                    Text(
                        text = "4. PILIH TIER PAKET SPESIFIKASI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        plans.forEach { plan ->
                            PlanSelectorCard(
                                plan = plan,
                                isSelected = plan.id == selectedPlanId,
                                onClick = { selectedPlanId = plan.id }
                            )
                        }
                    }
                }

                // Order Summary & Checkout
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 18.dp
                    ) {
                        Text(
                            text = "RINGKASAN PROVISIONING",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NixeonTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SummaryRow(label = "Nama Server", value = serverName.ifBlank { "(Belum diisi)" })
                        SummaryRow(label = "Software (Egg)", value = selectedEgg?.name ?: "-")
                        SummaryRow(label = "Node & Lokasi", value = "${selectedNode?.name} (${selectedNode?.location})")
                        SummaryRow(label = "Spesifikasi", value = "${(selectedPlan?.memoryMb ?: 0) / 1024} GB RAM • ${selectedPlan?.cpuPercent}% CPU • ${(selectedPlan?.diskMb ?: 0) / 1024} GB Disk")
                        Divider(color = NixeonGlassBorder, modifier = Modifier.padding(vertical = 8.dp))

                        SummaryRow(
                            label = "Biaya Bulan Pertama",
                            value = "Rp${String.format(Locale.GERMAN, "%,d", selectedPlan?.pricePerMonth ?: 0L)}",
                            isBold = true,
                            valueColor = NixeonCyan
                        )
                        SummaryRow(
                            label = "Saldo Nixeon Anda",
                            value = "Rp${String.format(Locale.GERMAN, "%,d", userBalance)}",
                            valueColor = if (canAfford) NixeonEmerald else NixeonCrimson
                        )

                        if (!canAfford) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Saldo tidak mencukupi. Silakan lakukan Top-Up terlebih dahulu.",
                                color = NixeonCrimson,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = NixeonCrimson,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LiquidPrimaryButton(
                            text = if (isDeploying) "Mengalokasikan ke Wings Node..." else "Deploy Server Sekarang",
                            loading = isDeploying,
                            enabled = canAfford && serverName.isNotBlank() && !isDeploying,
                            icon = Icons.Default.RocketLaunch,
                            onClick = {
                                isDeploying = true
                                errorMessage = null
                                val result = repository.createServer(
                                    actor = currentAccount,
                                    targetAccountId = currentAccount.id,
                                    serverName = serverName.trim(),
                                    planId = selectedPlanId,
                                    eggId = selectedEggId,
                                    nodeId = selectedNodeId
                                )
                                isDeploying = false
                                result.fold(
                                    onSuccess = { server ->
                                        onServerCreated(server)
                                    },
                                    onFailure = { ex ->
                                        errorMessage = ex.message ?: "Gagal membuat server."
                                    }
                                )
                            },
                            testTag = "deploy_server_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EggSelectorTile(
    egg: Egg,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) NixeonCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) NixeonCyan else Color.White.copy(alpha = 0.1f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = egg.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) NixeonCyan else NixeonTextPrimary
                    )
                )
                Text(
                    text = "${egg.dockerImage} • Port: ${egg.defaultPort}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NixeonTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = NixeonCyan,
                    unselectedColor = NixeonGlassBorder
                )
            )
        }
    }
}

@Composable
private fun NodeSelectorTile(
    node: ServerNode,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) NixeonViolet.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) NixeonViolet else Color.White.copy(alpha = 0.1f)
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) NixeonViolet else NixeonTextPrimary
                )
            )
            Text(
                text = "${node.location} • Ping ~15ms",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NixeonTextSecondary,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${node.allocations.count { !it.isAssigned }} alokasi siap",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NixeonEmerald,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun PlanSelectorCard(
    plan: Plan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) NixeonCyan else Color.White.copy(alpha = 0.12f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) NixeonCyan else NixeonTextPrimary
                    )
                )
                Text(
                    text = "${plan.memoryMb / 1024} GB RAM • ${plan.cpuPercent}% CPU • ${plan.diskMb / 1024} GB SSD",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NixeonTextSecondary,
                        fontSize = 11.5.sp
                    )
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Rp${String.format(Locale.GERMAN, "%,d", plan.pricePerMonth)}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = NixeonTextPrimary
                    )
                )
                Text(
                    text = "/bulan",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NixeonTextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
