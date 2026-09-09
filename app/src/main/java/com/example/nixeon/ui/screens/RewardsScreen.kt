package com.example.nixeon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.data.model.RewardProgram
import com.example.nixeon.data.model.UserAccount
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.LiquidBackground
import com.example.nixeon.ui.components.LiquidBadge
import com.example.nixeon.ui.components.LiquidGlassCard
import com.example.nixeon.ui.components.LiquidPrimaryButton
import com.example.nixeon.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onBack: () -> Unit
) {
    val rewardPrograms by repository.rewardPrograms.collectAsState()
    var voucherCodeInput by remember { mutableStateOf("") }
    var actionFeedback by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Hadiah & Program Bansos", fontWeight = FontWeight.Bold, color = NixeonTextPrimary) },
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Redeem Voucher Card
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 18.dp
                    ) {
                        Text(
                            text = "REDEEM KODE VOUCHER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NixeonTextSecondary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = voucherCodeInput,
                            onValueChange = { voucherCodeInput = it.uppercase() },
                            placeholder = { Text("Contoh: MERDEKA2026 atau MARRSPACESPECIAL", color = NixeonTextMuted) },
                            leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = NixeonEmerald) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voucher_code_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = NixeonTextPrimary,
                                unfocusedTextColor = NixeonTextPrimary,
                                focusedBorderColor = NixeonEmerald,
                                unfocusedBorderColor = NixeonGlassBorder,
                                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LiquidPrimaryButton(
                            text = "Klaim Voucher",
                            icon = Icons.Default.Celebration,
                            onClick = {
                                if (voucherCodeInput.isNotBlank()) {
                                    val result = repository.redeemVoucher(currentAccount.id, voucherCodeInput.trim())
                                    result.fold(
                                        onSuccess = { amountAdded ->
                                            isSuccess = true
                                            actionFeedback = "Selamat! Voucher senilai Rp${String.format(Locale.GERMAN, "%,d", amountAdded)} telah ditambahkan ke saldo Anda."
                                            voucherCodeInput = ""
                                        },
                                        onFailure = { ex ->
                                            isSuccess = false
                                            actionFeedback = ex.message ?: "Voucher gagal diklaim."
                                        }
                                    )
                                }
                            }
                        )

                        if (actionFeedback != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = actionFeedback ?: "",
                                color = if (isSuccess) NixeonEmerald else NixeonCrimson,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Reward Programs & Bansos
                item {
                    Text(
                        text = "PROGRAM BANTUAN SOSIAL & EVENT NIXEON",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                items(rewardPrograms) { program ->
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = program.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonTextPrimary
                                    )
                                )
                                Text(
                                    text = program.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NixeonTextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Manfaat: Rp${String.format(Locale.GERMAN, "%,d", program.grantAmount)} Saldo Nixeon",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NixeonAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    val res = repository.claimRewardProgram(currentAccount.id, program.id)
                                    res.fold(
                                        onSuccess = {
                                            isSuccess = true
                                            actionFeedback = "Berhasil klaim program ${program.title}!"
                                        },
                                        onFailure = { ex ->
                                            isSuccess = false
                                            actionFeedback = ex.message ?: "Gagal klaim program."
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NixeonAmber,
                                    contentColor = NixeonObsidian
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Klaim", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
