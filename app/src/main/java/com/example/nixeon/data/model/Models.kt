package com.example.nixeon.data.model

enum class AdminSlot(val displayName: String) {
    ADM_01("adm_01 (Admin Utama)"),
    ADM_02("adm_02 (Admin Cadangan/Support)")
}

enum class NixeonRole {
    USER,
    ADMIN
}

enum class PanelRole(val label: String) {
    USER("Pterodactyl User"),
    ADMIN("Pterodactyl Admin")
}

enum class AdminScope(val title: String, val description: String) {
    SUPPORT_ADMIN("Support Admin", "Melihat profil, server, tiket & status server."),
    BILLING_ADMIN("Billing Admin", "Mengelola saldo, approve/reject top-up, voucher, bonus & bansos."),
    PANEL_ADMIN("Panel Admin", "Mengelola user panel, node, egg, resource, server, live console & file."),
    PLATFORM_ADMIN("Platform Admin (All-Access)", "Akses menyeluruh terhadap active user, akun panel user/admin, live server semua user, console, file, audit log, backup & migrasi VPS.")
}

enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    BANNED
}

data class UserAccount(
    val id: String,
    val email: String,
    val name: String,
    val role: NixeonRole,
    val adminSlot: AdminSlot? = null,
    val panelRole: PanelRole = PanelRole.USER,
    val scopes: List<AdminScope> = emptyList(),
    val status: AccountStatus = AccountStatus.ACTIVE,
    val pterodactylUserId: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val activationToken: String? = null,
    val isActivated: Boolean = true,
    val phone: String = ""
) {
    val isOfficialAdmin: Boolean get() = adminSlot != null
    val hasPlatformAdmin: Boolean get() = scopes.contains(AdminScope.PLATFORM_ADMIN)
    val hasBillingAdmin: Boolean get() = hasPlatformAdmin || scopes.contains(AdminScope.BILLING_ADMIN)
    val hasPanelAdmin: Boolean get() = hasPlatformAdmin || scopes.contains(AdminScope.PANEL_ADMIN)
    val hasSupportAdmin: Boolean get() = hasPlatformAdmin || scopes.contains(AdminScope.SUPPORT_ADMIN)
    val activationCode: String? get() = activationToken
}

data class BalanceRecord(
    val accountId: String,
    val balance: Long,
    val holdBalance: Long = 0L,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

enum class LedgerType(val label: String, val isCredit: Boolean) {
    TOPUP_CREDIT("Top-Up Masuk (Disetujui)", true),
    SERVER_HOLD("Hold Saldo Pembuatan Server", false),
    SERVER_COMMIT("Biaya Pembuatan Server", false),
    SERVER_REFUND("Refund Hold Saldo Gagal", true),
    PLAN_PURCHASE("Pembelian Paket Server", false),
    PLAN_RENEWAL("Perpanjangan Masa Aktif Server", false),
    PLAN_UPGRADE("Upgrade Spesifikasi Server", false),
    VOUCHER_CREDIT("Redeem Voucher", true),
    ADMIN_GRANT("Grant Saldo dari Admin", true),
    BONUS_EVENT("Bonus & Bansos Nixeon", true),
    REVERSAL_DEBIT("Koreksi Ledger (Pembalik Debet)", false),
    REVERSAL_CREDIT("Koreksi Ledger (Pembalik Kredit)", true)
}

data class LedgerEntry(
    val id: String,
    val accountId: String,
    val type: LedgerType,
    val amount: Long,
    val reference: String,
    val idempotencyKey: String,
    val description: String,
    val createdBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class EntitlementStatus(val label: String) {
    ACTIVE("Aktif"),
    GRACE_PERIOD("Masa Tenggang (Grace Period)"),
    SUSPENDED("Ditangguhkan (Suspended)"),
    EXPIRED("Kadaluarsa")
}

data class Plan(
    val id: String,
    val name: String,
    val price: Long,
    val memoryMb: Int,
    val cpuPercent: Int,
    val diskMb: Int,
    val maxServers: Int,
    val backups: Int,
    val databases: Int,
    val allowedEggIds: List<Int>,
    val billingPeriodDays: Int = 30,
    val active: Boolean = true,
    val description: String = ""
) {
    val pricePerMonth: Long get() = price
    val maxBackups: Int get() = backups
    val maxDatabases: Int get() = databases
}

data class ResourceEntitlement(
    val accountId: String,
    val planId: String,
    val startsAt: Long,
    val expiresAt: Long,
    val gracePeriodDays: Int = 3,
    val status: EntitlementStatus = EntitlementStatus.ACTIVE,
    val grantedBy: String? = null,
    val customMemoryMb: Int? = null,
    val customCpuPercent: Int? = null,
    val customDiskMb: Int? = null,
    val customMaxServers: Int? = null
)

data class Egg(
    val id: Int,
    val name: String,
    val category: String,
    val dockerImage: String,
    val startupCommand: String,
    val description: String,
    val defaultMemoryMb: Int = 1024,
    val defaultPort: Int = 25565
)

data class NodeAllocation(
    val id: Int,
    val ip: String,
    val port: Int,
    val isAssigned: Boolean = false
)

data class ServerNode(
    val id: Int,
    val name: String,
    val fqdn: String,
    val location: String,
    val totalMemoryMb: Int,
    val usedMemoryMb: Int,
    val isOnline: Boolean = true,
    val allocations: List<NodeAllocation> = listOf(
        NodeAllocation(1, fqdn, 25565, false),
        NodeAllocation(2, fqdn, 25566, false),
        NodeAllocation(3, fqdn, 25567, true)
    )
)

enum class ServerStatus(val label: String) {
    RUNNING("Online"),
    STOPPED("Offline"),
    STARTING("Memulai..."),
    STOPPING("Menghentikan..."),
    OFFLINE("Offline"),
    PROVISIONING("Provisioning..."),
    SUSPENDED("Suspended")
}

data class NixeonServer(
    val id: String,
    val accountId: String,
    val ownerEmail: String,
    val pterodactylServerId: String,
    val name: String,
    val eggId: Int,
    val eggName: String,
    val nodeId: Int,
    val nodeName: String,
    val allocationIp: String,
    val allocationPort: Int,
    val memoryMb: Int,
    val cpuPercent: Int,
    val diskMb: Int,
    val status: ServerStatus,
    val planId: String,
    val createdAt: Long,
    val suspendedAt: Long? = null,
    val cpuUsagePercent: Float = 0f,
    val ramUsageMb: Float = 0f,
    val diskUsageMb: Float = 0f,
    val uptimeSeconds: Long = 0L
)

data class ConsoleLogLine(
    val timestamp: Long = System.currentTimeMillis(),
    val text: String,
    val isCommand: Boolean = false,
    val level: String = "INFO"
)

data class ServerFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val modifiedAt: Long = System.currentTimeMillis(),
    val content: String? = null
) {
    val contentPreview: String? get() = content
}

enum class TopUpMethod(val label: String) {
    QRIS("QRIS Statis Nixeon"),
    DANA("DANA Official Marrspace")
}

enum class TopUpStatus(val label: String) {
    PENDING("Menunggu Review Admin"),
    APPROVED("Disetujui (Saldo Masuk)"),
    REJECTED("Ditolak"),
    CANCELLED("Dibatalkan")
}

data class TopUpTransaction(
    val id: String,
    val accountId: String,
    val accountEmail: String,
    val method: TopUpMethod,
    val amount: Long,
    val reference: String,
    val idempotencyKey: String,
    val status: TopUpStatus,
    val proofFileId: String? = null,
    val senderAccountName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null,
    val rejectionReason: String? = null
) {
    val senderName: String get() = senderAccountName
}

data class Voucher(
    val code: String,
    val amount: Long,
    val quota: Int,
    val usedCount: Int,
    val expiresAt: Long,
    val active: Boolean = true
)

enum class ProgramType {
    DAILY_BONUS,
    BANSOS_STARTER,
    FESTIVAL_EVENT
}

data class RewardProgram(
    val id: String,
    val title: String,
    val description: String,
    val type: ProgramType,
    val rewardAmount: Long? = null,
    val rewardDays: Int? = null,
    val isActive: Boolean = true,
    val claimedAccountIds: List<String> = emptyList()
) {
    val grantAmount: Long get() = rewardAmount ?: 0L
}

data class AdminActionLog(
    val id: String,
    val actorId: String,
    val actorName: String,
    val scope: String,
    val targetType: String,
    val targetId: String,
    val action: String,
    val reason: String? = null,
    val result: String = "SUCCESS",
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

data class NotificationItem(
    val id: String,
    val accountId: String,
    val title: String,
    val body: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    val message: String get() = body
    val createdAt: Long get() = timestamp
}

data class BackupManifest(
    val format: String = "nixeon-backup-v1",
    val createdAt: String,
    val createdBy: String,
    val schemaVersion: Int = 7,
    val encrypted: Boolean = true,
    val checksumSha256: String,
    val includes: List<String>,
    val sizeKb: Long
) {
    val archiveName: String get() = "nixeon_snapshot_v${schemaVersion}_encrypted.tar.gz"
    val encryptionMethod: String get() = "AES-256-GCM"
}

enum class VpsMigrationPhase(val label: String) {
    H_14_INVENTORY("H-14: Inventaris Node, Allocation, Server & DB"),
    H_7_PREPARATION("H-7: Backup Panel & Wings Volumes, Persiapan VPS Baru"),
    H_1_CUTOVER("H-1: Maintenance Mode, Final Snapshot & Staging Rehearsal"),
    MAINTENANCE_CUTOVER("H-1: Maintenance Mode, Final Snapshot & Staging Rehearsal"),
    COMPLETED("Selesai: DNS Switch, Rotasi Token Pterodactyl & Observasi")
}

data class VpsMigrationState(
    val phase: VpsMigrationPhase = VpsMigrationPhase.H_14_INVENTORY,
    val isMaintenanceMode: Boolean = false,
    val activeNodes: Int = 2,
    val panelDbBackedUp: Boolean = true,
    val businessDbBackedUp: Boolean = true,
    val wingsVolumesBackedUp: Boolean = true,
    val stagingRestored: Boolean = false,
    val reconciliationPassed: Boolean = false,
    val dnsUpdated: Boolean = false,
    val tokenRotated: Boolean = false,
    val targetVpsIp: String = "103.189.201.44",
    val lastCutoverAt: Long? = null
) {
    val currentPhase: VpsMigrationPhase get() = phase
    val targetHost: String get() = targetVpsIp
}
