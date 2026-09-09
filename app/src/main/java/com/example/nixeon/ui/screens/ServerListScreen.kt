package com.example.nixeon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nixeon.data.model.NixeonServer
import com.example.nixeon.data.model.UserAccount
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.components.LiquidBackground
import com.example.nixeon.ui.components.LiquidPrimaryButton
import com.example.nixeon.ui.theme.NixeonCyan
import com.example.nixeon.ui.theme.NixeonGlassBorder
import com.example.nixeon.ui.theme.NixeonTextPrimary
import com.example.nixeon.ui.theme.NixeonTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    repository: NixeonRepository,
    currentAccount: UserAccount,
    onBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onServerClick: (NixeonServer) -> Unit
) {
    val servers by repository.servers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val userServers = remember(servers, searchQuery, currentAccount.id) {
        servers.filter { it.accountId == currentAccount.id }
            .filter {
                if (searchQuery.isBlank()) true
                else it.name.contains(searchQuery, ignoreCase = true) || it.eggName.contains(searchQuery, ignoreCase = true)
            }
    }

    LiquidBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Server Saya", fontWeight = FontWeight.Bold, color = NixeonTextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = NixeonTextPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToCreate) {
                            Icon(Icons.Default.Add, contentDescription = "Buat Server", tint = NixeonCyan)
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
                    .padding(horizontal = 18.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama server atau software egg...", color = NixeonTextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NixeonCyan) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("server_search_input"),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = NixeonTextPrimary,
                        unfocusedTextColor = NixeonTextPrimary,
                        focusedBorderColor = NixeonCyan,
                        unfocusedBorderColor = NixeonGlassBorder,
                        focusedContainerColor = Color.White.copy(alpha = 0.04f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (userServers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Tidak ada server yang cocok.", color = NixeonTextSecondary)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
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
    }
}
