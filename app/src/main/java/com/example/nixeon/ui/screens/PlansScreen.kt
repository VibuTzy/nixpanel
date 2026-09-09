package com.example.nixeon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.data.model.Plan
import com.example.nixeon.data.model.UserAccount
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.LiquidBackground
import com.example.nixeon.ui.components.LiquidBadge
import com.example.nixeon.ui.components.LiquidGlassCard
import com.example.nixeon.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onBack: () -> Unit
) {
    val plans by repository.plans.collectAsState()
    val entitlements by repository.entitlements.collectAsState()
    val currentEntitlement = entitlements[currentAccount.id]

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Paket & Entitlement Hosting", fontWeight = FontWeight.Bold, color = NixeonTextPrimary) },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Text(
                        text = "TIER PAKET SPESIFIKASI CLOUD NIXEON",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                items(plans) { plan ->
                    val isCurrent = plan.id == currentEntitlement?.planId

                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isCurrent) NixeonCyan else Color.White.copy(alpha = 0.15f),
                        contentPadding = 18.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) NixeonCyan else NixeonTextPrimary
                                    )
                                )
                                Text(
                                    text = "Rp${String.format(Locale.GERMAN, "%,d", plan.pricePerMonth)} / bulan",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = NixeonTextPrimary
                                    )
                                )
                            }

                            if (isCurrent) {
                                LiquidBadge(
                                    text = "PAKET AKTIF ANDA",
                                    badgeColor = NixeonCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        SpecFeatureRow("Alokasi RAM Maksimal: ${plan.memoryMb / 1024} GB")
                        SpecFeatureRow("Batas CPU: ${plan.cpuPercent}% Dedicated Wings")
                        SpecFeatureRow("Kapasitas NVMe SSD: ${plan.diskMb / 1024} GB")
                        SpecFeatureRow("Maksimal Slot Server: ${plan.maxServers} server")
                        SpecFeatureRow("Database MySQL: ${plan.maxDatabases} database")
                        SpecFeatureRow("Slot Backup Arkais: ${plan.maxBackups} snapshot")
                        SpecFeatureRow("Grace Period Kadaluarsa: 7 Hari")
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecFeatureRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = NixeonEmerald, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary))
    }
}
