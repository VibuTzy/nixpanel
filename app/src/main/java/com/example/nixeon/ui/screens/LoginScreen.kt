package com.example.nixeon.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.nixeon.data.model.UserAccount
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.LiquidBackground
import com.example.nixeon.ui.components.LiquidGlassCard
import com.example.nixeon.ui.components.LiquidPrimaryButton
import com.example.nixeon.ui.theme.*

@Composable
fun LoginScreen(
    repository: NixeonRepository,
    onLoginSuccess: (UserAccount) -> Unit
) {
    var email by remember { mutableStateOf("user@nixeon.id") }
    var password by remember { mutableStateOf("User123!") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isActivationMode by remember { mutableStateOf(false) }
    var activationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LiquidBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Icon & Brand Title
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_nixeon_logo),
                    contentDescription = "Nixeon Logo",
                    modifier = Modifier.size(68.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "NIXEON",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NixeonCyan
                )
            )

            Text(
                text = "Native Liquid Glass Cloud & Panel Gateway",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = NixeonTextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Policy Notice Card
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFF13283F).copy(alpha = 0.6f),
                borderColor = NixeonCyan.copy(alpha = 0.3f),
                contentPadding = 14.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NixeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Akses Resmi: Hanya memiliki 2 akun admin (adm_01 & adm_02). Akun user hanya dapat dibuat oleh admin.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NixeonTextPrimary,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login / Activation Card
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
                        text = if (isActivationMode) "Aktivasi Akun Baru" else "Masuk Akun",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextPrimary
                        )
                    )

                    TextButton(
                        onClick = {
                            isActivationMode = !isActivationMode
                            errorMessage = null
                            successMessage = null
                        }
                    ) {
                        Text(
                            text = if (isActivationMode) "Kembali ke Login" else "Punya Kode Aktivasi?",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NixeonCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isActivationMode) {
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Akun Nixeon", color = NixeonTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = NixeonCyan)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NixeonTextPrimary,
                            unfocusedTextColor = NixeonTextPrimary,
                            focusedBorderColor = NixeonCyan,
                            unfocusedBorderColor = NixeonGlassBorder,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kata Sandi", color = NixeonTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NixeonCyan)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = NixeonTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NixeonTextPrimary,
                            unfocusedTextColor = NixeonTextPrimary,
                            focusedBorderColor = NixeonCyan,
                            unfocusedBorderColor = NixeonGlassBorder,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    // Activation Mode
                    Text(
                        text = "Masukkan kode token aktivasi yang diberikan oleh adm_01 atau adm_02 saat pembuatan akun Anda.",
                        style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = activationCode,
                        onValueChange = { activationCode = it },
                        label = { Text("Kode Token Aktivasi", color = NixeonTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NixeonAmber)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NixeonTextPrimary,
                            unfocusedTextColor = NixeonTextPrimary,
                            focusedBorderColor = NixeonAmber,
                            unfocusedBorderColor = NixeonGlassBorder,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Buat Kata Sandi Baru Anda", color = NixeonTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.LockReset, contentDescription = null, tint = NixeonCyan)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NixeonTextPrimary,
                            unfocusedTextColor = NixeonTextPrimary,
                            focusedBorderColor = NixeonCyan,
                            unfocusedBorderColor = NixeonGlassBorder,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = NixeonCrimson,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = successMessage ?: "",
                        color = NixeonEmerald,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                LiquidPrimaryButton(
                    text = if (isActivationMode) "Aktifkan & Masuk" else "Masuk ke Nixeon",
                    loading = isLoading,
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        successMessage = null
                        if (isActivationMode) {
                            val result = repository.activateAccount(activationCode, newPassword)
                            isLoading = false
                            result.fold(
                                onSuccess = { acc ->
                                    successMessage = "Akun berhasil diaktifkan!"
                                    onLoginSuccess(acc)
                                },
                                onFailure = { ex ->
                                    errorMessage = ex.message ?: "Aktivasi gagal."
                                }
                            )
                        } else {
                            val result = repository.login(email, password)
                            isLoading = false
                            result.fold(
                                onSuccess = { acc ->
                                    onLoginSuccess(acc)
                                },
                                onFailure = { ex ->
                                    errorMessage = ex.message ?: "Login gagal."
                                }
                            )
                        }
                    },
                    testTag = "login_submit_button"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Demo Switcher Card for testing all roles & slots
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Text(
                    text = "PILIH AKUN ROLE CEPAT (PENGUJIAN):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NixeonTextSecondary,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // adm_01 Chip
                QuickAccountChip(
                    title = "adm_01 (Admin Utama)",
                    subtitle = "Platform Admin (All-Access) & Billing",
                    badgeText = "OFFICIAL ADMIN 1",
                    badgeColor = NixeonAmber,
                    onClick = {
                        email = "admin1@nixeon.id"
                        password = "Admin123!"
                        isActivationMode = false
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // adm_02 Chip
                QuickAccountChip(
                    title = "adm_02 (Admin Cadangan / Support)",
                    subtitle = "Billing, Panel & Support Admin",
                    badgeText = "OFFICIAL ADMIN 2",
                    badgeColor = NixeonViolet,
                    onClick = {
                        email = "admin2@nixeon.id"
                        password = "Admin123!"
                        isActivationMode = false
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // usr_1001 Chip
                QuickAccountChip(
                    title = "usr_1001 (Customer Pelanggan)",
                    subtitle = "User biasa, saldo Rp65.000, 2 server aktif",
                    badgeText = "USER NORMAL",
                    badgeColor = NixeonEmerald,
                    onClick = {
                        email = "user@nixeon.id"
                        password = "User123!"
                        isActivationMode = false
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // usr_1002 Chip (User with Panel Role Admin to test rule #6)
                QuickAccountChip(
                    title = "usr_1002 (Panel Role Admin)",
                    subtitle = "Role panel admin, TIDAK berhak Nixeon Admin Console",
                    badgeText = "PANEL ADMIN USER",
                    badgeColor = NixeonCyan,
                    onClick = {
                        email = "tester@nixeon.id"
                        password = "User123!"
                        isActivationMode = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "Nixeon v1.0.0 • Secured by Marrspace & Yowtech | ren`z",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = NixeonTextMuted
                )
            )
        }
    }
}

@Composable
private fun QuickAccountChip(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextPrimary
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NixeonTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = NixeonTextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
