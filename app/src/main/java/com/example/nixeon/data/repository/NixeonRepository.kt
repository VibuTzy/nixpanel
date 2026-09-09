package com.example.nixeon.data.repository

import android.content.Context
import androidx.room.Room
import com.example.nixeon.data.gateway.NixeonGateway
import com.example.nixeon.data.local.*
import com.example.nixeon.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class NixeonRepository(
    private val context: Context,
    private val gateway: NixeonGateway = NixeonGateway.getInstance()
) {
    private val database: NixeonDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            NixeonDatabase::class.java,
            "nixeon_local_cache.db"
        ).fallbackToDestructiveMigration().build()
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    // Observables from Gateway
    val currentSession: StateFlow<UserAccount?> = gateway.currentSession
    val accounts: StateFlow<Map<String, UserAccount>> = gateway.accounts
    val balances: StateFlow<Map<String, BalanceRecord>> = gateway.balances
    val ledgers: StateFlow<List<LedgerEntry>> = gateway.ledgers
    val plans: StateFlow<List<Plan>> = gateway.plans
    val entitlements: StateFlow<Map<String, ResourceEntitlement>> = gateway.entitlements
    val eggs: StateFlow<List<Egg>> = gateway.eggs
    val nodes: StateFlow<List<ServerNode>> = gateway.nodes
    val servers: StateFlow<List<NixeonServer>> = gateway.servers
    val topUps: StateFlow<List<TopUpTransaction>> = gateway.topUps
    val vouchers: StateFlow<List<Voucher>> = gateway.vouchers
    val rewardPrograms: StateFlow<List<RewardProgram>> = gateway.rewardPrograms
    val auditLogs: StateFlow<List<AdminActionLog>> = gateway.auditLogs
    val notifications: StateFlow<List<NotificationItem>> = gateway.notifications
    val vpsMigration: StateFlow<VpsMigrationState> = gateway.vpsMigration

    init {
        // Sync Gateway changes into Room Cache in background
        scope.launch {
            gateway.servers.collect { serverList ->
                val entities = serverList.map { s ->
                    CachedServerEntity(
                        id = s.id,
                        accountId = s.accountId,
                        name = s.name,
                        eggName = s.eggName,
                        allocation = "${s.allocationIp}:${s.allocationPort}",
                        memoryMb = s.memoryMb,
                        cpuPercent = s.cpuPercent,
                        diskMb = s.diskMb,
                        status = s.status.name,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                database.serverDao().insertServers(entities)
            }
        }

        scope.launch {
            gateway.balances.collect { balMap ->
                balMap.values.forEach { b ->
                    database.balanceDao().saveBalance(
                        CachedBalanceEntity(
                            accountId = b.accountId,
                            balance = b.balance,
                            holdBalance = b.holdBalance,
                            lastSyncedAt = b.lastSyncedAt
                        )
                    )
                }
            }
        }
    }

    // Cached room access for offline mode
    fun getCachedServers(accountId: String): Flow<List<CachedServerEntity>> {
        return database.serverDao().getServersForAccount(accountId)
    }

    fun getCachedBalance(accountId: String): Flow<CachedBalanceEntity?> {
        return database.balanceDao().getBalance(accountId)
    }

    // ==========================================
    // DELEGATED GATEWAY METHODS
    // ==========================================

    fun login(email: String, pass: String): Result<UserAccount> = gateway.login(email, pass)
    fun logout() = gateway.logout()
    fun switchAccount(accountId: String): Result<UserAccount> = gateway.switchAccount(accountId)
    fun activateAccount(code: String, pass: String): Result<UserAccount> = gateway.activateAccount(code, pass)

    fun submitTopUp(accountId: String, method: TopUpMethod, amount: Long, reference: String, senderName: String, proofFileId: String?) =
        gateway.submitTopUp(accountId, method, amount, reference, senderName, proofFileId)

    fun approveTopUp(adminActor: UserAccount, topUpId: String) =
        gateway.approveTopUp(adminActor, topUpId)

    fun rejectTopUp(adminActor: UserAccount, topUpId: String, reason: String) =
        gateway.rejectTopUp(adminActor, topUpId, reason)

    fun redeemVoucher(accountId: String, voucherCode: String) =
        gateway.redeemVoucher(accountId, voucherCode)

    fun claimRewardProgram(accountId: String, programId: String) =
        gateway.claimRewardProgram(accountId, programId)

    fun createServer(actor: UserAccount, targetAccountId: String, serverName: String, planId: String, eggId: Int, nodeId: Int) =
        gateway.createServer(actor, targetAccountId, serverName, planId, eggId, nodeId)

    fun performServerPowerAction(actor: UserAccount, serverId: String, action: String) =
        gateway.performServerPowerAction(actor, serverId, action)

    fun sendConsoleCommand(actor: UserAccount, serverId: String, command: String) =
        gateway.sendConsoleCommand(actor, serverId, command)

    fun getConsoleLogs(serverId: String) = gateway.getConsoleLogs(serverId)

    fun getServerFiles(serverId: String) = gateway.getServerFiles(serverId)

    fun deleteServerFile(actor: UserAccount, serverId: String, path: String) =
        gateway.deleteServerFile(actor, serverId, path)

    fun saveServerFile(actor: UserAccount, serverId: String, path: String, content: String) =
        gateway.saveServerFile(actor, serverId, path, content)

    fun adminCreateUser(
        adminActor: UserAccount,
        email: String,
        name: String,
        panelRole: PanelRole,
        planId: String,
        initialGrantAmount: Long,
        allowedEggIds: List<Int>,
        activeDays: Int,
        gracePeriodDays: Int
    ) = gateway.adminCreateUser(adminActor, email, name, panelRole, planId, initialGrantAmount, allowedEggIds, activeDays, gracePeriodDays)

    fun adminAdjustBalance(adminActor: UserAccount, targetAccountId: String, amountDelta: Long, reason: String) =
        gateway.adminAdjustBalance(adminActor, targetAccountId, amountDelta, reason)

    fun generateBackupArchive(adminActor: UserAccount) = gateway.generateBackupArchive(adminActor)

    fun restoreBackupDryRun(adminActor: UserAccount, manifest: BackupManifest) =
        gateway.restoreBackupDryRun(adminActor, manifest)

    fun updateVpsMigrationStep(adminActor: UserAccount, newPhase: VpsMigrationPhase, toggleMaintenance: Boolean? = null) =
        gateway.updateVpsMigrationStep(adminActor, newPhase, toggleMaintenance)

    fun markNotificationAsRead(id: String) = gateway.markNotificationAsRead(id)
}
