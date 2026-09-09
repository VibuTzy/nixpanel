package com.example.nixeon.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun TopUpScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onBack: () -> Unit
) {
    val topUps by repository.topUps.collectAsState()
    val userTopUps = topUps.filter { it.accountId == currentAccount.id }

    var selectedMethod by remember { mutableStateOf(TopUpMethod.QRIS) }
    var selectedAmount by remember { mutableStateOf(50000L) }
    var customAmountText by remember { mutableStateOf("") }
    var referenceNumber by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf(currentAccount.name) }
    var proofFileName by remember { mutableStateOf<String?>("bukti_bayar_${System.currentTimeMillis().toString().takeLast(4)}.jpg") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Top-Up Saldo Nixeon",
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
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
                // Rule & Security Banner
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFF13283F).copy(alpha = 0.5f),
                        borderColor = NixeonCyan.copy(alpha = 0.3f),
                        contentPadding = 12.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NixeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "PENTING: Saldo tidak bertambah dari input aplikasi. Verifikasi dilakukan oleh admin melalui ledger append-only.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NixeonTextPrimary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                // Method Selector Tabs
                item {
                    Text(
                        text = "1. PILIH METODE PEMBAYARAN",
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
                        PaymentMethodTab(
                            title = "QRIS Statis",
                            subtitle = "Semua Bank & E-Wallet",
                            icon = Icons.Default.QrCodeScanner,
                            isSelected = selectedMethod == TopUpMethod.QRIS,
                            color = NixeonCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedMethod = TopUpMethod.QRIS }
                        )

                        PaymentMethodTab(
                            title = "DANA Transfer",
                            subtitle = "Official Marrspace",
                            icon = Icons.Default.AccountBalanceWallet,
                            isSelected = selectedMethod == TopUpMethod.DANA,
                            color = NixeonViolet,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedMethod = TopUpMethod.DANA }
                        )
                    }
                }

                // Payment Visual & Guide Card
                item {
                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 18.dp
                    ) {
                        if (selectedMethod == TopUpMethod.QRIS) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "QRIS STATIS RESMI NIXEON",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonCyan,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "NMD: Nixeon Cloud Marrspace Nusantara",
                                    style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // QR Code Illustration Canvas
                                Surface(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    color = Color.White
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Canvas(modifier = Modifier.size(130.dp)) {
                                            val step = size.width / 9f
                                            // Mock matrix blocks
                                            drawRect(Color.Black, Offset(0f, 0f), Size(step * 3, step * 3))
                                            drawRect(Color.White, Offset(step, step), Size(step, step))

                                            drawRect(Color.Black, Offset(step * 6, 0f), Size(step * 3, step * 3))
                                            drawRect(Color.White, Offset(step * 7, step), Size(step, step))

                                            drawRect(Color.Black, Offset(0f, step * 6), Size(step * 3, step * 3))
                                            drawRect(Color.White, Offset(step, step * 7), Size(step, step))

                                            // Central data blocks
                                            drawRect(Color(0xFF07111F), Offset(step * 4, step * 4), Size(step * 2, step * 2))
                                            drawRect(Color.Black, Offset(step * 4, step), Size(step, step * 2))
                                            drawRect(Color.Black, Offset(step, step * 4), Size(step * 2, step))
                                            drawRect(Color.Black, Offset(step * 6, step * 4), Size(step * 2, step))
                                            drawRect(Color.Black, Offset(step * 4, step * 7), Size(step, step * 2))
                                        }
                                        Text(
                                            text = "QRIS",
                                            fontWeight = FontWeight.Black,
                                            color = NixeonCyan,
                                            fontSize = 11.sp,
                                            modifier = Modifier
                                                .background(Color.Black, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Scan menggunakan m-BCA, Mandiri, BRImo, DANA, GoPay, OVO, atau ShopeePay",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NixeonTextMuted,
                                        fontSize = 11.5.sp
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            // DANA Account Details
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = NixeonViolet,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "AKUN DANA RESMI NIXEON",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NixeonViolet,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        Text(
                                            text = "A/N: Yowtech | ren`z (Marrspace)",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextPrimary)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.05f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NixeonViolet.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Nomor DANA Official:",
                                                style = MaterialTheme.typography.labelSmall.copy(color = NixeonTextSecondary)
                                            )
                                            Text(
                                                text = "0857-4300-3734",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Black,
                                                    color = NixeonCyan,
                                                    letterSpacing = 1.sp
                                                )
                                            )
                                        }
                                        LiquidBadge(
                                            text = "TERVERIFIKASI",
                                            badgeColor = NixeonEmerald
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sertakan email Anda '${currentAccount.email}' pada catatan transfer DANA.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NixeonTextMuted,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Nominal Selector
                item {
                    Text(
                        text = "2. NOMINAL TOP-UP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val amounts = listOf(10000L, 20000L, 50000L, 100000L, 200000L)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        amounts.take(3).forEach { amt ->
                            NominalChip(
                                amount = amt,
                                isSelected = selectedAmount == amt && customAmountText.isEmpty(),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedAmount = amt
                                    customAmountText = ""
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        amounts.drop(3).forEach { amt ->
                            NominalChip(
                                amount = amt,
                                isSelected = selectedAmount == amt && customAmountText.isEmpty(),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedAmount = amt
                                    customAmountText = ""
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = {
                            customAmountText = it.filter { char -> char.isDigit() }
                            if (customAmountText.isNotEmpty()) {
                                selectedAmount = customAmountText.toLongOrNull() ?: 10000L
                            }
                        },
                        label = { Text("Atau Masukkan Nominal Bebas (Rp)", color = NixeonTextSecondary) },
                        leadingIcon = {
                            Text("Rp", fontWeight = FontWeight.Bold, color = NixeonCyan, modifier = Modifier.padding(start = 12.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NixeonTextPrimary,
                            unfocusedTextColor = NixeonTextPrimary,
                            focusedBorderColor = NixeonCyan,
                            unfocusedBorderColor = NixeonGlassBorder,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        )
                    )
                }

                // Reference & Sender Form
                item {
                    Text(
                        text = "3. DETAIL BUKTI & KONFIRMASI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LiquidGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 16.dp
                    ) {
                        OutlinedTextField(
                            value = referenceNumber,
                            onValueChange = { referenceNumber = it },
                            label = { Text("Nomor Referensi Transfer / RRN", color = NixeonTextSecondary) },
                            placeholder = { Text("Contoh: QRIS-2026-9901 atau TRX-12345", color = NixeonTextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("topup_reference_input"),
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

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = senderName,
                            onValueChange = { senderName = it },
                            label = { Text("Nama Pemilik Rekening Pengirim", color = NixeonTextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Proof Attachment
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.04f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NixeonGlassBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = NixeonEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Lampiran Bukti Struk:",
                                            style = MaterialTheme.typography.labelSmall.copy(color = NixeonTextSecondary)
                                        )
                                        Text(
                                            text = proofFileName ?: "Belum dilampirkan",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = NixeonTextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        proofFileName = "struk_transfer_${System.currentTimeMillis().toString().takeLast(6)}.jpg"
                                    }
                                ) {
                                    Text("Ganti File", color = NixeonCyan)
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = NixeonCrimson,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (successMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = successMessage ?: "",
                                color = NixeonEmerald,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LiquidPrimaryButton(
                            text = "Ajukan Verifikasi Top-Up (Rp${String.format(Locale.GERMAN, "%,d", selectedAmount)})",
                            loading = isLoading,
                            icon = Icons.Default.Send,
                            onClick = {
                                if (referenceNumber.isBlank()) {
                                    errorMessage = "Nomor referensi pembayaran wajib diisi."
                                    return@LiquidPrimaryButton
                                }
                                isLoading = true
                                errorMessage = null
                                successMessage = null

                                val result = repository.submitTopUp(
                                    accountId = currentAccount.id,
                                    method = selectedMethod,
                                    amount = selectedAmount,
                                    reference = referenceNumber,
                                    senderName = senderName,
                                    proofFileId = proofFileName
                                )
                                isLoading = false
                                result.fold(
                                    onSuccess = {
                                        successMessage = "Pengajuan top-up berhasil dibuat (Status: Menunggu Review Admin)."
                                        referenceNumber = ""
                                    },
                                    onFailure = { ex ->
                                        errorMessage = ex.message ?: "Gagal membuat pengajuan."
                                    }
                                )
                            },
                            testTag = "submit_topup_button"
                        )
                    }
                }

                // History of Submissions
                item {
                    Text(
                        text = "STATUS PENGAJUAN TOP-UP ANDA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                if (userTopUps.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada riwayat pengajuan top-up.",
                            style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextMuted)
                        )
                    }
                } else {
                    items(userTopUps) { tx ->
                        TopUpItemCard(tx = tx)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopUpItemCard(tx: TopUpTransaction) {
    val dateStr = remember(tx.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(tx.createdAt))
    }

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
                    text = "Rp${String.format(Locale.GERMAN, "%,d", tx.amount)} • ${tx.method.label}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NixeonTextPrimary
                    )
                )
                Text(
                    text = "Ref: ${tx.reference} • $dateStr",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NixeonTextSecondary,
                        fontSize = 11.5.sp
                    )
                )
                if (!tx.rejectionReason.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Alasan Penolakan: ${tx.rejectionReason}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NixeonCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            LiquidBadge(
                text = tx.status.label,
                badgeColor = when (tx.status) {
                    TopUpStatus.PENDING -> NixeonAmber
                    TopUpStatus.APPROVED -> NixeonEmerald
                    TopUpStatus.REJECTED -> NixeonCrimson
                    TopUpStatus.CANCELLED -> Color.Gray
                },
                textColor = if (tx.status == TopUpStatus.PENDING) NixeonObsidian else Color.White
            )
        }
    }
}

@Composable
private fun PaymentMethodTab(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) color else Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else NixeonTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) color else NixeonTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NixeonTextMuted,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun NominalChip(
    amount: Long,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) NixeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) NixeonCyan else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Rp${String.format(Locale.GERMAN, "%,d", amount)}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) NixeonCyan else NixeonTextPrimary
                )
            )
        }
    }
}
