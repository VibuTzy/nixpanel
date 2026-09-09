package com.example.nixeon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.data.model.LedgerEntry
import com.example.nixeon.data.model.UserAccount
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.LiquidBackground
import com.example.nixeon.ui.components.LiquidBadge
import com.example.nixeon.ui.components.LiquidGlassCard
import com.example.nixeon.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onBack: () -> Unit
) {
    val ledgers by repository.ledgers.collectAsState()
    val userLedgers = ledgers.filter { it.accountId == currentAccount.id }

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Riwayat Ledger Transaksi", fontWeight = FontWeight.Bold, color = NixeonTextPrimary) },
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Text(
                        text = "LEDGER RESMI APPEND-ONLY (MUTASI SALDO)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                if (userLedgers.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada mutasi ledger saldo.", color = NixeonTextSecondary)
                        }
                    }
                } else {
                    items(userLedgers) { entry ->
                        LedgerEntryCard(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerEntryCard(entry: LedgerEntry) {
    val dateStr = remember(entry.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))
    }
    val isCredit = entry.amount >= 0

    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NixeonTextPrimary
                    )
                )
                Text(
                    text = "Ref: ${entry.reference} • Aktor: ${entry.createdBy}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NixeonTextMuted,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NixeonTextSecondary,
                        fontSize = 10.5.sp
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else ""}Rp${String.format(Locale.GERMAN, "%,d", entry.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isCredit) NixeonEmerald else NixeonCrimson
                    )
                )
                LiquidBadge(
                    text = entry.type.name.replace("_", " "),
                    badgeColor = if (isCredit) NixeonEmerald.copy(alpha = 0.2f) else NixeonCrimson.copy(alpha = 0.2f),
                    textColor = if (isCredit) NixeonEmerald else NixeonCrimson
                )
            }
        }
    }
}
