package com.example.nixeon.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nixeon.ui.components.LiquidBackground
import com.example.nixeon.ui.components.LiquidBadge
import com.example.nixeon.ui.components.LiquidGlassCard
import com.example.nixeon.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val openWhatsApp = { number: String ->
        val cleanNumber = number.replace("+", "").replace("-", "").replace(" ", "")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNumber"))
        context.startActivity(intent)
    }

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bantuan & Kontak Support", fontWeight = FontWeight.Bold, color = NixeonTextPrimary) },
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
                item {
                    Text(
                        text = "KONTAK RESMI OPERATOR NIXEON",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NixeonTextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // WhatsApp 1: Yowtech | ren`z
                item {
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
                                    text = "Yowtech | ren`z",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonCyan
                                    )
                                )
                                Text(
                                    text = "Billing, Top-Up & Technical Support Official",
                                    style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary)
                                )
                                Text(
                                    text = "+62 857-4300-3734",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonTextPrimary
                                    )
                                )
                            }

                            Button(
                                onClick = { openWhatsApp("+6285743003734") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NixeonEmerald,
                                    contentColor = NixeonObsidian
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chat WA", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // WhatsApp 2: Marrspace Support
                item {
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
                                    text = "Marrspace Support",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonViolet
                                    )
                                )
                                Text(
                                    text = "Infrastructure, VPS & Node Allocation",
                                    style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextSecondary)
                                )
                                Text(
                                    text = "+60 175-174-8749",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NixeonTextPrimary
                                    )
                                )
                            }

                            Button(
                                onClick = { openWhatsApp("+601751748749") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NixeonEmerald,
                                    contentColor = NixeonObsidian
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chat WA", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Infrastructure Status
                item {
                    Text(
                        text = "STATUS LAYANAN CLOUD NIXEON",
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
                        StatusRow(service = "Nixeon Backend Gateway HTTPS", status = "OPERATIONAL")
                        Divider(color = NixeonGlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                        StatusRow(service = "Wings Node SG-01 (Singapore)", status = "OPERATIONAL")
                        Divider(color = NixeonGlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                        StatusRow(service = "Wings Node JKT-01 (Cyber IDC)", status = "OPERATIONAL")
                        Divider(color = NixeonGlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                        StatusRow(service = "Ledger Engine & Database", status = "OPERATIONAL")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(service: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NixeonEmerald, modifier = Modifier.size(18.dp))
            Text(service, style = MaterialTheme.typography.bodySmall.copy(color = NixeonTextPrimary))
        }
        LiquidBadge(text = status, badgeColor = NixeonEmerald)
    }
}
