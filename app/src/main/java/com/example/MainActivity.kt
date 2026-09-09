package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.nixeon.data.model.UserAccount
import com.example.nixeon.data.repository.NixeonRepository
import com.example.nixeon.ui.screens.*
import com.example.nixeon.ui.theme.NixeonObsidian
import com.example.nixeon.ui.theme.NixeonTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: NixeonRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = NixeonRepository(applicationContext)

        setContent {
            NixeonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NixeonObsidian
                ) {
                    val navController = rememberNavController()
                    val currentSession by repository.currentSession.collectAsState()

                    NavHost(
                        navController = navController,
                        startDestination = if (currentSession != null) "home" else "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                repository = repository,
                                onLoginSuccess = { user ->
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            val user = currentSession
                            if (user == null) {
                                LaunchedEffect(Unit) {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            } else {
                                UserHomeScreen(
                                    repository = repository,
                                    currentAccount = user,
                                    onNavigateToServers = { navController.navigate("servers") },
                                    onNavigateToCreateServer = { navController.navigate("create_server") },
                                    onNavigateToTopUp = { navController.navigate("top_up") },
                                    onNavigateToPlans = { navController.navigate("plans") },
                                    onNavigateToRewards = { navController.navigate("rewards") },
                                    onNavigateToHistory = { navController.navigate("history") },
                                    onNavigateToSupport = { navController.navigate("support") },
                                    onNavigateToNotifications = { navController.navigate("notifications") },
                                    onNavigateToAdminConsole = { navController.navigate("admin_console") },
                                    onServerClick = { server ->
                                        navController.navigate("server_detail/${server.id}")
                                    },
                                    onLogout = {
                                        repository.logout()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        composable("servers") {
                            val user = currentSession ?: return@composable
                            ServerListScreen(
                                repository = repository,
                                currentAccount = user,
                                onBack = { navController.popBackStack() },
                                onNavigateToCreate = { navController.navigate("create_server") },
                                onServerClick = { server ->
                                    navController.navigate("server_detail/${server.id}")
                                }
                            )
                        }

                        composable(
                            route = "server_detail/{serverId}",
                            arguments = listOf(navArgument("serverId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val user = currentSession ?: return@composable
                            val serverId = backStackEntry.arguments?.getString("serverId") ?: ""
                            ServerDetailScreen(
                                repository = repository,
                                currentAccount = user,
                                serverId = serverId,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("create_server") {
                            val user = currentSession ?: return@composable
                            CreateServerScreen(
                                repository = repository,
                                currentAccount = user,
                                onBack = { navController.popBackStack() },
                                onServerCreated = { newServer ->
                                    navController.navigate("server_detail/${newServer.id}") {
                                        popUpTo("create_server") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("top_up") {
                            val user = currentSession ?: return@composable
                            TopUpScreen(
                                repository = repository,
                                currentAccount = user,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("plans") {
                            val user = currentSession ?: return@composable
                            PlansScreen(
                                repository = repository,
                                currentAccount = user,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("rewards") {
                            val user = currentSession ?: return@composable
                            RewardsScreen(
                                repository = repository,
                                currentAccount = user,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("history") {
                            val user = currentSession ?: return@composable
                            TransactionHistoryScreen(
                                repository = repository,
                                currentAccount = user,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("support") {
                            SupportScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("notifications") {
                            val user = currentSession ?: return@composable
                            NotificationListScreen(
                                repository = repository,
                                currentAccount = user,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("admin_console") {
                            val user = currentSession ?: return@composable
                            AdminConsoleScreen(
                                repository = repository,
                                currentAccount = user,
                                onBack = { navController.popBackStack() },
                                onServerClick = { server ->
                                    navController.navigate("server_detail/${server.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
