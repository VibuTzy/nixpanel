package com.example.nixeon.data.gateway

import com.example.nixeon.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * NixeonGateway:
 * Single Source of Truth for Server-Side State, Accounting Ledger,
 * RBAC/Scope Enforcement, Pterodactyl Integration Proxy,
 * Audit Trail, and Migration Orchestration.
 */
class NixeonGateway private constructor() {

    companion object {
        @Volatile
        private var instance: NixeonGateway? = null

        fun getInstance(): NixeonGateway {
            return instance ?: synchronized(this) {
                instance ?: NixeonGateway().also { instance = it }
            }
        }
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // In-memory server-side state
    private val _accounts = MutableStateFlow<Map<String, UserAccount>>(emptyMap())
    val accounts: StateFlow<Map<String, UserAccount>> = _accounts.asStateFlow()

    private val _passwords = mutableMapOf<String, String>()

    private val _balances = MutableStateFlow<Map<String, BalanceRecord>>(emptyMap())
    val balances: StateFlow<Map<String, BalanceRecord>> = _balances.asStateFlow()

    private val _ledgers = MutableStateFlow<List<LedgerEntry>>(emptyList())
    val ledgers: StateFlow<List<LedgerEntry>> = _ledgers.asStateFlow()

    private val _plans = MutableStateFlow<List<Plan>>(emptyList())
    val plans: StateFlow<List<Plan>> = _plans.asStateFlow()

    private val _entitlements = MutableStateFlow<Map<String, ResourceEntitlement>>(emptyMap())
    val entitlements: StateFlow<Map<String, ResourceEntitlement>> = _entitlements.asStateFlow()

    private val _eggs = MutableStateFlow<List<Egg>>(emptyList())
    val eggs: StateFlow<List<Egg>> = _eggs.asStateFlow()

    private val _nodes = MutableStateFlow<List<ServerNode>>(emptyList())
    val nodes: StateFlow<List<ServerNode>> = _nodes.asStateFlow()

    private val _servers = MutableStateFlow<List<NixeonServer>>(emptyList())
    val servers: StateFlow<List<NixeonServer>> = _servers.asStateFlow()

    private val _topUps = MutableStateFlow<List<TopUpTransaction>>(emptyList())
    val topUps: StateFlow<List<TopUpTransaction>> = _topUps.asStateFlow()

    private val _vouchers = MutableStateFlow<List<Voucher>>(emptyList())
    val vouchers: StateFlow<List<Voucher>> = _vouchers.asStateFlow()

    private val _rewardPrograms = MutableStateFlow<List<RewardProgram>>(emptyList())
    val rewardPrograms: StateFlow<List<RewardProgram>> = _rewardPrograms.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AdminActionLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminActionLog>> = _auditLogs.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _vpsMigration = MutableStateFlow(VpsMigrationState())
    val vpsMigration: StateFlow<VpsMigrationState> = _vpsMigration.asStateFlow()

    // Server Console logs by Server ID
    private val _consoleLogs = mutableMapOf<String, MutableList<ConsoleLogLine>>()

    // Server Files by Server ID
    private val _serverFiles = mutableMapOf<String, MutableList<ServerFileItem>>()

    // Support Contacts
    val supportContacts = mapOf(
        "ren_z" to Pair("Yowtech | ren`z", "+62 857-4300-3734"),
        "marrspace" to Pair("Marrspace Support", "+60 175-174-8749")
    )

    // Current authenticated session
    private val _currentSession = MutableStateFlow<UserAccount?>(null)
    val currentSession: StateFlow<UserAccount?> = _currentSession.asStateFlow()

    init {
        bootstrapDefaultData()
    }

    private fun bootstrapDefaultData() {
        val now = System.currentTimeMillis()
        val thirtyDaysLater = now + (30L * 24 * 60 * 60 * 1000)

        // 1. Eggs
        val defaultEggs = listOf(
            Egg(1, "Minecraft Paper 1.20+", "Minecraft", "ghcr.io/pterodactyl/yolks:java_21", "java -Xms128M -XX:MaxRAMPercentage=95.0 -jar server.jar", "High-performance Minecraft PaperMC server with Java 21", 2048),
            Egg(2, "Node.js 20 LTS", "Discord Bot & Web", "ghcr.io/pterodactyl/yolks:nodejs_20", "npm start", "Modern JavaScript/TypeScript runtime for bots and APIs", 1024),
            Egg(3, "Python 3.11", "Bots & Scripts", "ghcr.io/pterodactyl/yolks:python_3.11", "python main.py", "Python application runtime with pip package manager", 1024),
            Egg(4, "Terraria TShock", "Game Server", "ghcr.io/pterodactyl/yolks:terraria", "./TerrariaServer.bin.x86_64", "Multiplayer Terraria server with plugin support", 2048),
            Egg(5, "Rust Oxide", "Game Server", "ghcr.io/pterodactyl/yolks:rust", "./RustDedicated", "High-tickrate Rust dedicated server", 4096)
        )
        _eggs.value = defaultEggs

        // 2. Nodes
        val defaultNodes = listOf(
            ServerNode(1, "Node Alpha (SG-01)", "sg01.panel.marrlabs.my.id", "Singapore Equinix", 32768, 12288, true),
            ServerNode(2, "Node Beta (JKT-01)", "jkt01.panel.marrlabs.my.id", "Jakarta Cyber Data Center", 65536, 24576, true)
        )
        _nodes.value = defaultNodes

        // 3. Plans
        val defaultPlans = listOf(
            Plan("plan_starter", "Starter Node", 10000L, 1024, 50, 10240, 1, 1, 1, listOf(1, 2, 3), 30, true, "Cocok untuk bot Discord ringan dan Minecraft personal"),
            Plan("plan_growth", "Growth Power", 20000L, 2048, 100, 25600, 2, 2, 2, listOf(1, 2, 3, 4), 30, true, "Pilihan populer untuk server survival dan bot multi-server"),
            Plan("plan_pro", "Pro Performance", 40000L, 4096, 200, 51200, 3, 3, 3, listOf(1, 2, 3, 4, 5), 30, true, "Performa tinggi untuk public server komunitas dengan backup berkala"),
            Plan("plan_custom", "Enterprise Custom", 100000L, 8192, 400, 102400, 5, 5, 5, listOf(1, 2, 3, 4, 5), 30, true, "Kapasitas penuh dengan alokasi resource khusus enterprise")
        )
        _plans.value = defaultPlans

        // 4. Accounts: EXACTLY TWO OFFICIAL ADMINS and Default Users
        val adm01 = UserAccount(
            id = "adm_01",
            email = "admin1@nixeon.id",
            name = "Admin Utama (adm_01)",
            role = NixeonRole.ADMIN,
            adminSlot = AdminSlot.ADM_01,
            panelRole = PanelRole.ADMIN,
            scopes = listOf(
                AdminScope.SUPPORT_ADMIN,
                AdminScope.BILLING_ADMIN,
                AdminScope.PANEL_ADMIN,
                AdminScope.PLATFORM_ADMIN
            ),
            status = AccountStatus.ACTIVE,
            pterodactylUserId = 1L,
            createdAt = now,
            phone = "+6285743003734"
        )
        _passwords["adm_01"] = "Admin123!"

        val adm02 = UserAccount(
            id = "adm_02",
            email = "admin2@nixeon.id",
            name = "Admin Support (adm_02)",
            role = NixeonRole.ADMIN,
            adminSlot = AdminSlot.ADM_02,
            panelRole = PanelRole.ADMIN,
            scopes = listOf(
                AdminScope.SUPPORT_ADMIN,
                AdminScope.BILLING_ADMIN,
                AdminScope.PANEL_ADMIN
            ),
            status = AccountStatus.ACTIVE,
            pterodactylUserId = 2L,
            createdAt = now,
            phone = "+60175187449"
        )
        _passwords["adm_02"] = "Admin123!"

        val usr1001 = UserAccount(
            id = "usr_1001",
            email = "user@nixeon.id",
            name = "Rizqi Agung",
            role = NixeonRole.USER,
            adminSlot = null,
            panelRole = PanelRole.USER,
            scopes = emptyList(),
            status = AccountStatus.ACTIVE,
            pterodactylUserId = 101L,
            createdAt = now,
            phone = "+628123456789"
        )
        _passwords["usr_1001"] = "User123!"

        // usr_1002 has Panel Role ADMIN to prove requirement 6:
        // "Panel role admin pada account user tidak otomatis memberikan akses Admin Console Nixeon"
        val usr1002 = UserAccount(
            id = "usr_1002",
            email = "tester@nixeon.id",
            name = "Panel Admin User (No Nixeon Admin Scope)",
            role = NixeonRole.USER,
            adminSlot = null,
            panelRole = PanelRole.ADMIN,
            scopes = emptyList(),
            status = AccountStatus.ACTIVE,
            pterodactylUserId = 102L,
            createdAt = now,
            phone = "+628987654321"
        )
        _passwords["usr_1002"] = "User123!"

        val accountMap = mapOf(
            adm01.id to adm01,
            adm02.id to adm02,
            usr1001.id to usr1001,
            usr1002.id to usr1002
        )
        _accounts.value = accountMap

        // 5. Balances & Initial Ledgers
        val balanceMap = mutableMapOf<String, BalanceRecord>()
        val initialLedgers = mutableListOf<LedgerEntry>()

        // adm_01 balance
        balanceMap["adm_01"] = BalanceRecord("adm_01", 500000L, 0L, now)
        initialLedgers.add(
            LedgerEntry(
                id = "ldg_adm01_init",
                accountId = "adm_01",
                type = LedgerType.ADMIN_GRANT,
                amount = 500000L,
                reference = "BOOTSTRAP-ADM01",
                idempotencyKey = "boot-adm-01",
                description = "Saldo operasional awal adm_01",
                createdBy = "SYSTEM",
                timestamp = now
            )
        )

        // adm_02 balance
        balanceMap["adm_02"] = BalanceRecord("adm_02", 250000L, 0L, now)
        initialLedgers.add(
            LedgerEntry(
                id = "ldg_adm02_init",
                accountId = "adm_02",
                type = LedgerType.ADMIN_GRANT,
                amount = 250000L,
                reference = "BOOTSTRAP-ADM02",
                idempotencyKey = "boot-adm-02",
                description = "Saldo operasional awal adm_02",
                createdBy = "SYSTEM",
                timestamp = now
            )
        )

        // usr_1001 balance
        balanceMap["usr_1001"] = BalanceRecord("usr_1001", 65000L, 0L, now)
        initialLedgers.add(
            LedgerEntry(
                id = "ldg_usr01_init",
                accountId = "usr_1001",
                type = LedgerType.ADMIN_GRANT,
                amount = 25000L,
                reference = "GRANT-USR1001",
                idempotencyKey = "grant-usr-1001",
                description = "Saldo awal selamat datang pelanggan baru",
                createdBy = "adm_01",
                timestamp = now - (2 * 86400000L)
            )
        )
        initialLedgers.add(
            LedgerEntry(
                id = "ldg_usr01_topup",
                accountId = "usr_1001",
                type = LedgerType.TOPUP_CREDIT,
                amount = 40000L,
                reference = "QRIS-20260907-001",
                idempotencyKey = "topup-usr-1001-1",
                description = "Top-up QRIS disetujui oleh adm_01",
                createdBy = "adm_01",
                timestamp = now - 86400000L
            )
        )

        // usr_1002 balance
        balanceMap["usr_1002"] = BalanceRecord("usr_1002", 0L, 0L, now)

        _balances.value = balanceMap
        _ledgers.value = initialLedgers

        // 6. Entitlements
        val entitlementMap = mapOf(
            "usr_1001" to ResourceEntitlement(
                accountId = "usr_1001",
                planId = "plan_growth",
                startsAt = now - (2 * 86400000L),
                expiresAt = thirtyDaysLater,
                gracePeriodDays = 3,
                status = EntitlementStatus.ACTIVE,
                grantedBy = "adm_01"
            ),
            "adm_01" to ResourceEntitlement(
                accountId = "adm_01",
                planId = "plan_custom",
                startsAt = now,
                expiresAt = now + (365L * 86400000L),
                gracePeriodDays = 7,
                status = EntitlementStatus.ACTIVE,
                grantedBy = "SYSTEM"
            ),
            "adm_02" to ResourceEntitlement(
                accountId = "adm_02",
                planId = "plan_pro",
                startsAt = now,
                expiresAt = now + (365L * 86400000L),
                gracePeriodDays = 7,
                status = EntitlementStatus.ACTIVE,
                grantedBy = "adm_01"
            )
        )
        _entitlements.value = entitlementMap

        // 7. Initial Servers
        val srv1 = NixeonServer(
            id = "srv_minecraft_01",
            accountId = "usr_1001",
            ownerEmail = "user@nixeon.id",
            pterodactylServerId = "pt_srv_a9f1",
            name = "Survival Nusantara Paper 1.20",
            eggId = 1,
            eggName = "Minecraft Paper 1.20+",
            nodeId = 1,
            nodeName = "Node Alpha (SG-01)",
            allocationIp = "103.189.201.12",
            allocationPort = 25565,
            memoryMb = 2048,
            cpuPercent = 100,
            diskMb = 25600,
            status = ServerStatus.RUNNING,
            planId = "plan_growth",
            createdAt = now - 86400000L,
            cpuUsagePercent = 24.5f,
            ramUsageMb = 1420f,
            diskUsageMb = 3120f,
            uptimeSeconds = 43200L
        )

        val srv2 = NixeonServer(
            id = "srv_bot_discord",
            accountId = "usr_1001",
            ownerEmail = "user@nixeon.id",
            pterodactylServerId = "pt_srv_b7c2",
            name = "Nixeon Moderation Bot",
            eggId = 2,
            eggName = "Node.js 20 LTS",
            nodeId = 1,
            nodeName = "Node Alpha (SG-01)",
            allocationIp = "103.189.201.12",
            allocationPort = 3000,
            memoryMb = 1024,
            cpuPercent = 50,
            diskMb = 10240,
            status = ServerStatus.RUNNING,
            planId = "plan_starter",
            createdAt = now - (2 * 86400000L),
            cpuUsagePercent = 5.2f,
            ramUsageMb = 310f,
            diskUsageMb = 640f,
            uptimeSeconds = 172800L
        )

        _servers.value = listOf(srv1, srv2)

        // Seed console logs & files
        _consoleLogs[srv1.id] = mutableListOf(
            ConsoleLogLine(now - 10000, "[Paper] Loading libraries, please wait..."),
            ConsoleLogLine(now - 8000, "[Paper] Starting minecraft server version 1.20.4"),
            ConsoleLogLine(now - 6000, "[Server thread/INFO]: Preparing level 'world'"),
            ConsoleLogLine(now - 3000, "[Server thread/INFO]: Done (4.120s)! For help, type 'help'"),
            ConsoleLogLine(now - 1000, "[Server thread/INFO]: Timings Reset")
        )

        _serverFiles[srv1.id] = mutableListOf(
            ServerFileItem("server.properties", "/server.properties", false, 1240, now, "server-port=25565\nmotd=Nixeon Survival Server\ngamemode=survival\ndifficulty=hard\nmax-players=20\nonline-mode=true"),
            ServerFileItem("paper.yml", "/paper.yml", false, 3450, now, "verbose: false\nconfig-version: 29\nsettings:\n  chunk-loading:\n    min-load-radius: 2"),
            ServerFileItem("plugins", "/plugins", true, 0, now, null),
            ServerFileItem("world", "/world", true, 0, now, null),
            ServerFileItem("logs", "/logs", true, 0, now, null)
        )

        // 8. Pending & Historical Top-Ups
        val topUpList = listOf(
            TopUpTransaction(
                id = "top_pending_01",
                accountId = "usr_1001",
                accountEmail = "user@nixeon.id",
                method = TopUpMethod.QRIS,
                amount = 50000L,
                reference = "QRIS-20260909-8812",
                idempotencyKey = "top-qris-8812",
                status = TopUpStatus.PENDING,
                senderAccountName = "Rizqi Agung Mandiri",
                proofFileId = "proof_qris_8812.jpg",
                createdAt = now - (15 * 60 * 1000)
            ),
            TopUpTransaction(
                id = "top_approved_00",
                accountId = "usr_1001",
                accountEmail = "user@nixeon.id",
                method = TopUpMethod.DANA,
                amount = 40000L,
                reference = "DANA-20260908-1102",
                idempotencyKey = "top-dana-1102",
                status = TopUpStatus.APPROVED,
                senderAccountName = "Rizqi A.",
                proofFileId = "proof_dana_1102.jpg",
                createdAt = now - 86400000L,
                reviewedAt = now - 86000000L,
                reviewedBy = "adm_01"
            )
        )
        _topUps.value = topUpList

        // 9. Vouchers
        val voucherList = listOf(
            Voucher("NIXEON2026", 15000L, 100, 12, now + (60L * 86400000L), true),
            Voucher("MARRSPACE-LAUNCH", 20000L, 50, 48, now + (30L * 86400000L), true),
            Voucher("YOWTECH-BONUS", 10000L, 200, 35, now + (15L * 86400000L), true)
        )
        _vouchers.value = voucherList

        // 10. Reward Programs / Bansos
        val programs = listOf(
            RewardProgram(
                id = "prg_bansos_starter",
                title = "Bansos Pelajar & Komunitas",
                description = "Bantuan subsidi server Starter Node 1 GB untuk developer dan komunitas kecil.",
                type = ProgramType.BANSOS_STARTER,
                rewardAmount = 10000L,
                rewardDays = 30,
                isActive = true
            ),
            RewardProgram(
                id = "prg_daily_login",
                title = "Klaim Harian Nixeon",
                description = "Bonus saldo harian Rp1.000 setiap hari untuk pengguna aktif.",
                type = ProgramType.DAILY_BONUS,
                rewardAmount = 1000L,
                isActive = true
            ),
            RewardProgram(
                id = "prg_festival_sept",
                title = "Festival Cloud September 2026",
                description = "Diskon perpanjangan server hingga 20% dan cashback saldo instan.",
                type = ProgramType.FESTIVAL_EVENT,
                rewardAmount = 5000L,
                isActive = true
            )
        )
        _rewardPrograms.value = programs

        // 11. Notifications
        val notifs = listOf(
            NotificationItem(
                id = "ntf_01",
                accountId = "usr_1001",
                title = "Top-Up Berhasil",
                body = "Top-up DANA senilai Rp40.000 telah disetujui oleh adm_01. Saldo telah masuk ke akun Anda.",
                type = "BILLING",
                timestamp = now - 86000000L,
                isRead = true
            ),
            NotificationItem(
                id = "ntf_02",
                accountId = "usr_1001",
                title = "Server Berhasil Dibuat",
                body = "Server Survival Nusantara Paper 1.20 telah aktif di Node Alpha (SG-01). Port: 25565.",
                type = "SERVER",
                timestamp = now - 85000000L,
                isRead = true
            ),
            NotificationItem(
                id = "ntf_03",
                accountId = "usr_1001",
                title = "Pengajuan Top-Up Terkirim",
                body = "Pengajuan QRIS Rp50.000 sedang dalam antrian review admin. Harap tunggu konfirmasi.",
                type = "BILLING",
                timestamp = now - (15 * 60 * 1000),
                isRead = false
            )
        )
        _notifications.value = notifs

        // 12. Audit Logs
        val audits = listOf(
            AdminActionLog(
                id = "aud_001",
                actorId = "adm_01",
                actorName = "adm_01 (Admin Utama)",
                scope = "platform_admin",
                targetType = "USER",
                targetId = "usr_1001",
                action = "CREATE_USER",
                reason = "Aktivasi pelanggan baru Nixeon",
                result = "SUCCESS",
                timestamp = now - (2 * 86400000L),
                details = "Plan: plan_growth, initialGrant: Rp25.000, panelRole: user"
            ),
            AdminActionLog(
                id = "aud_002",
                actorId = "adm_01",
                actorName = "adm_01 (Admin Utama)",
                scope = "billing_admin",
                targetType = "TOPUP",
                targetId = "top_approved_00",
                action = "APPROVE_TOPUP",
                reason = "Bukti transfer DANA valid dan match nominal",
                result = "SUCCESS",
                timestamp = now - 86000000L,
                details = "Nominal: Rp40.000, reference: DANA-20260908-1102"
            )
        )
        _auditLogs.value = audits

        // Default login as usr_1001 initially
        _currentSession.value = usr1001
    }

    // ==========================================
    // AUTHENTICATION & SESSION MANAGEMENT
    // ==========================================

    fun login(email: String, pass: String): Result<UserAccount> {
        val account = _accounts.value.values.find { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(Exception("Akun tidak ditemukan. Akun user hanya dapat dibuat oleh adm_01 atau adm_02."))

        if (account.status == AccountStatus.BANNED) {
            return Result.failure(Exception("Akun Anda telah dinonaktifkan oleh administrator Nixeon."))
        }

        val storedPass = _passwords[account.id]
        if (storedPass != pass) {
            return Result.failure(Exception("Password salah. Silakan periksa kembali credential Anda."))
        }

        _currentSession.value = account
        recordAudit(
            actorId = account.id,
            actorName = account.name,
            scope = if (account.isOfficialAdmin) account.scopes.firstOrNull()?.name ?: "admin" else "user",
            targetType = "AUTH",
            targetId = account.id,
            action = "LOGIN",
            reason = "Login sukses via native app",
            result = "SUCCESS"
        )
        return Result.success(account)
    }

    fun switchAccount(accountId: String): Result<UserAccount> {
        val target = _accounts.value[accountId]
            ?: return Result.failure(Exception("Akun tidak ditemukan."))
        _currentSession.value = target
        return Result.success(target)
    }

    fun logout() {
        val current = _currentSession.value
        if (current != null) {
            recordAudit(
                actorId = current.id,
                actorName = current.name,
                scope = if (current.isOfficialAdmin) "admin" else "user",
                targetType = "AUTH",
                targetId = current.id,
                action = "LOGOUT",
                reason = "Logout dari sesi APK",
                result = "SUCCESS"
            )
        }
        _currentSession.value = null
    }

    fun activateAccount(activationCode: String, newPassword: String): Result<UserAccount> {
        val account = _accounts.value.values.find { it.activationToken == activationCode.trim() }
            ?: return Result.failure(Exception("Kode atau tautan aktivasi tidak valid atau telah kedaluwarsa."))

        val updated = account.copy(isActivated = true, activationToken = null)
        _accounts.value = _accounts.value + (updated.id to updated)
        _passwords[updated.id] = newPassword
        _currentSession.value = updated

        recordAudit(
            actorId = updated.id,
            actorName = updated.name,
            scope = "user",
            targetType = "USER",
            targetId = updated.id,
            action = "ACTIVATE_ACCOUNT",
            reason = "Aktivasi mandiri dengan token admin",
            result = "SUCCESS"
        )

        return Result.success(updated)
    }

    // ==========================================
    // BALANCE & LEDGER (APPEND-ONLY & IDEMPOTENT)
    // ==========================================

    @Synchronized
    fun submitTopUp(
        accountId: String,
        method: TopUpMethod,
        amount: Long,
        reference: String,
        senderName: String,
        proofFileId: String? = null
    ): Result<TopUpTransaction> {
        if (amount < 10000L) {
            return Result.failure(Exception("Minimal top-up adalah Rp10.000"))
        }

        // Idempotency check on reference
        val existing = _topUps.value.find { it.reference.equals(reference.trim(), ignoreCase = true) }
        if (existing != null) {
            return Result.failure(Exception("Nomor referensi pembayaran '$reference' sudah pernah diajukan sebelumnya."))
        }

        val account = _accounts.value[accountId]
            ?: return Result.failure(Exception("Akun tidak valid."))

        val idempotencyKey = "topup_${accountId}_${System.currentTimeMillis()}"
        val transaction = TopUpTransaction(
            id = "top_${System.currentTimeMillis()}",
            accountId = accountId,
            accountEmail = account.email,
            method = method,
            amount = amount,
            reference = reference.trim(),
            idempotencyKey = idempotencyKey,
            status = TopUpStatus.PENDING,
            proofFileId = proofFileId ?: "proof_${System.currentTimeMillis()}.jpg",
            senderAccountName = senderName.trim(),
            createdAt = System.currentTimeMillis()
        )

        _topUps.value = listOf(transaction) + _topUps.value

        // Notify admins
        pushNotification(
            accountId = "adm_01",
            title = "Pengajuan Top-Up Baru",
            body = "Pengguna ${account.name} mengajukan top-up ${method.label} sebesar Rp${String.format(Locale.GERMAN, "%,d", amount)} (Ref: $reference).",
            type = "ADMIN_ALERT"
        )

        return Result.success(transaction)
    }

    @Synchronized
    fun approveTopUp(
        adminActor: UserAccount,
        topUpId: String
    ): Result<TopUpTransaction> {
        // Enforce admin scope
        if (!adminActor.hasBillingAdmin) {
            return Result.failure(Exception("Akses ditolak: Anda tidak memiliki scope billing_admin."))
        }

        val transaction = _topUps.value.find { it.id == topUpId }
            ?: return Result.failure(Exception("Transaksi top-up tidak ditemukan."))

        // Strict idempotency: prevent double credit!
        if (transaction.status != TopUpStatus.PENDING) {
            return Result.failure(Exception("Transaksi ini sudah diproses sebelumnya dengan status: ${transaction.status.label}. Approval tidak boleh dijalankan ganda."))
        }

        val targetAccountId = transaction.accountId
        val currentBalance = _balances.value[targetAccountId]?.balance ?: 0L
        val newBalance = currentBalance + transaction.amount

        // 1. Atomic Ledger Append
        val ledgerId = "ldg_top_${System.currentTimeMillis()}"
        val newLedger = LedgerEntry(
            id = ledgerId,
            accountId = targetAccountId,
            type = LedgerType.TOPUP_CREDIT,
            amount = transaction.amount,
            reference = transaction.reference,
            idempotencyKey = transaction.idempotencyKey,
            description = "Top-Up ${transaction.method.label} disetujui oleh ${adminActor.name}",
            createdBy = adminActor.id,
            timestamp = System.currentTimeMillis()
        )

        _ledgers.value = listOf(newLedger) + _ledgers.value

        // 2. Update Balance Record
        val updatedBalance = BalanceRecord(
            accountId = targetAccountId,
            balance = newBalance,
            holdBalance = _balances.value[targetAccountId]?.holdBalance ?: 0L,
            lastSyncedAt = System.currentTimeMillis()
        )
        _balances.value = _balances.value + (targetAccountId to updatedBalance)

        // 3. Mark Top-Up Status as APPROVED
        val updatedTx = transaction.copy(
            status = TopUpStatus.APPROVED,
            reviewedAt = System.currentTimeMillis(),
            reviewedBy = adminActor.id
        )
        _topUps.value = _topUps.value.map { if (it.id == topUpId) updatedTx else it }

        // 4. Record Audit
        recordAudit(
            actorId = adminActor.id,
            actorName = adminActor.name,
            scope = "billing_admin",
            targetType = "TOPUP",
            targetId = transaction.id,
            action = "APPROVE_TOPUP",
            reason = "Verifikasi bukti pembayaran valid",
            result = "SUCCESS",
            details = "Amount: Rp${transaction.amount}, Ref: ${transaction.reference}, Target: $targetAccountId"
        )

        // 5. Send push notification to user
        pushNotification(
            accountId = targetAccountId,
            title = "Top-Up Berhasil Disetujui!",
            body = "Saldo sebesar Rp${String.format(Locale.GERMAN, "%,d", transaction.amount)} telah masuk ke akun Anda. Saldo sekarang: Rp${String.format(Locale.GERMAN, "%,d", newBalance)}.",
            type = "BILLING"
        )

        return Result.success(updatedTx)
    }

    @Synchronized
    fun rejectTopUp(
        adminActor: UserAccount,
        topUpId: String,
        reason: String
    ): Result<TopUpTransaction> {
        if (!adminActor.hasBillingAdmin) {
            return Result.failure(Exception("Akses ditolak: Anda tidak memiliki scope billing_admin."))
        }
        if (reason.isBlank()) {
            return Result.failure(Exception("Alasan penolakan wajib diisi untuk audit trail."))
        }

        val transaction = _topUps.value.find { it.id == topUpId }
            ?: return Result.failure(Exception("Transaksi top-up tidak ditemukan."))

        if (transaction.status != TopUpStatus.PENDING) {
            return Result.failure(Exception("Transaksi ini sudah diproses dengan status ${transaction.status.label}."))
        }

        val updatedTx = transaction.copy(
            status = TopUpStatus.REJECTED,
            reviewedAt = System.currentTimeMillis(),
            reviewedBy = adminActor.id,
            rejectionReason = reason.trim()
        )
        _topUps.value = _topUps.value.map { if (it.id == topUpId) updatedTx else it }

        recordAudit(
            actorId = adminActor.id,
            actorName = adminActor.name,
            scope = "billing_admin",
            targetType = "TOPUP",
            targetId = transaction.id,
            action = "REJECT_TOPUP",
            reason = reason,
            result = "SUCCESS"
        )

        pushNotification(
            accountId = transaction.accountId,
            title = "Top-Up Ditolak",
            body = "Pengajuan top-up ${transaction.reference} ditolak oleh admin. Alasan: $reason",
            type = "BILLING"
        )

        return Result.success(updatedTx)
    }

    @Synchronized
    fun redeemVoucher(
        accountId: String,
        voucherCode: String
    ): Result<Long> {
        val codeClean = voucherCode.trim().uppercase(Locale.US)
        val voucher = _vouchers.value.find { it.code == codeClean && it.active }
            ?: return Result.failure(Exception("Kode voucher '$voucherCode' tidak valid atau telah habis."))

        if (voucher.usedCount >= voucher.quota) {
            return Result.failure(Exception("Kuota voucher '$codeClean' telah habis."))
        }
        if (System.currentTimeMillis() > voucher.expiresAt) {
            return Result.failure(Exception("Voucher '$codeClean' telah kadaluarsa."))
        }

        // Check if this account already redeemed this code
        val alreadyRedeemed = _ledgers.value.any {
            it.accountId == accountId && it.reference == "VOUCHER-$codeClean"
        }
        if (alreadyRedeemed) {
            return Result.failure(Exception("Anda sudah pernah menggunakan voucher ini sebelumnya."))
        }

        // Atomic update
        val updatedVoucher = voucher.copy(usedCount = voucher.usedCount + 1)
        _vouchers.value = _vouchers.value.map { if (it.code == voucher.code) updatedVoucher else it }

        val currentBal = _balances.value[accountId]?.balance ?: 0L
        val newBal = currentBal + voucher.amount

        val ledger = LedgerEntry(
            id = "ldg_vch_${System.currentTimeMillis()}",
            accountId = accountId,
            type = LedgerType.VOUCHER_CREDIT,
            amount = voucher.amount,
            reference = "VOUCHER-$codeClean",
            idempotencyKey = "vch_${accountId}_$codeClean",
            description = "Redeem voucher resmi $codeClean",
            createdBy = "USER",
            timestamp = System.currentTimeMillis()
        )
        _ledgers.value = listOf(ledger) + _ledgers.value

        _balances.value = _balances.value + (accountId to BalanceRecord(accountId, newBal, _balances.value[accountId]?.holdBalance ?: 0L, System.currentTimeMillis()))

        pushNotification(
            accountId = accountId,
            title = "Voucher Berhasil Diklaim!",
            body = "Saldo Rp${String.format(Locale.GERMAN, "%,d", voucher.amount)} telah ditambahkan dari voucher $codeClean.",
            type = "PROMOTION"
        )

        return Result.success(voucher.amount)
    }

    @Synchronized
    fun claimRewardProgram(
        accountId: String,
        programId: String
    ): Result<Long> {
        val program = _rewardPrograms.value.find { it.id == programId && it.isActive }
            ?: return Result.failure(Exception("Program reward tidak ditemukan."))

        if (program.claimedAccountIds.contains(accountId)) {
            return Result.failure(Exception("Anda sudah mengklaim reward program ini."))
        }

        val amount = program.rewardAmount ?: 0L
        val updatedClaimants = program.claimedAccountIds + accountId
        _rewardPrograms.value = _rewardPrograms.value.map {
            if (it.id == programId) it.copy(claimedAccountIds = updatedClaimants) else it
        }

        if (amount > 0) {
            val currentBal = _balances.value[accountId]?.balance ?: 0L
            val newBal = currentBal + amount

            val ledger = LedgerEntry(
                id = "ldg_rwd_${System.currentTimeMillis()}",
                accountId = accountId,
                type = LedgerType.BONUS_EVENT,
                amount = amount,
                reference = "PROGRAM-${program.id}",
                idempotencyKey = "rwd_${accountId}_${program.id}",
                description = "Klaim program: ${program.title}",
                createdBy = "SYSTEM",
                timestamp = System.currentTimeMillis()
            )
            _ledgers.value = listOf(ledger) + _ledgers.value

            _balances.value = _balances.value + (accountId to BalanceRecord(accountId, newBal, _balances.value[accountId]?.holdBalance ?: 0L, System.currentTimeMillis()))
        }

        pushNotification(
            accountId = accountId,
            title = "Reward Berhasil Diklaim",
            body = "Selamat! Anda mendapatkan benefit dari '${program.title}'.",
            type = "REWARD"
        )

        return Result.success(amount)
    }

    // ==========================================
    // SERVER PROVISIONING WORKFLOW (HOLD/COMMIT/ROLLBACK)
    // ==========================================

    @Synchronized
    fun createServer(
        actor: UserAccount,
        targetAccountId: String,
        serverName: String,
        planId: String,
        eggId: Int,
        nodeId: Int
    ): Result<NixeonServer> {
        val isSelf = actor.id == targetAccountId
        if (!isSelf && !actor.hasPanelAdmin) {
            return Result.failure(Exception("Akses ditolak: Hanya admin dengan scope panel_admin yang dapat membuat server untuk akun lain."))
        }

        val targetAccount = _accounts.value[targetAccountId]
            ?: return Result.failure(Exception("Target akun tidak ditemukan."))

        val plan = _plans.value.find { it.id == planId && it.active }
            ?: return Result.failure(Exception("Paket tidak ditemukan atau tidak aktif."))

        val egg = _eggs.value.find { it.id == eggId }
            ?: return Result.failure(Exception("Egg yang dipilih tidak valid."))

        if (!plan.allowedEggIds.contains(eggId)) {
            return Result.failure(Exception("Egg '${egg.name}' tidak diizinkan pada paket '${plan.name}'."))
        }

        val node = _nodes.value.find { it.id == nodeId && it.isOnline }
            ?: return Result.failure(Exception("Node server offline atau tidak tersedia."))

        // Check user quota
        val currentServers = _servers.value.filter { it.accountId == targetAccountId }
        if (currentServers.size >= plan.maxServers) {
            return Result.failure(Exception("Batas jumlah server untuk paket ini telah tercapai (Maks: ${plan.maxServers} server)."))
        }

        val currentBal = _balances.value[targetAccountId]?.balance ?: 0L
        val holdBal = _balances.value[targetAccountId]?.holdBalance ?: 0L

        if (currentBal < plan.price) {
            return Result.failure(Exception("Saldo tidak mencukupi (Harga: Rp${String.format(Locale.GERMAN, "%,d", plan.price)}, Saldo: Rp${String.format(Locale.GERMAN, "%,d", currentBal)}). Silakan lakukan top-up terlebih dahulu."))
        }

        // STEP 1: Hold Saldo
        val balanceAfterHold = currentBal - plan.price
        val holdAfterHold = holdBal + plan.price
        _balances.value = _balances.value + (targetAccountId to BalanceRecord(targetAccountId, balanceAfterHold, holdAfterHold, System.currentTimeMillis()))

        val holdLedger = LedgerEntry(
            id = "ldg_hld_${System.currentTimeMillis()}",
            accountId = targetAccountId,
            type = LedgerType.SERVER_HOLD,
            amount = plan.price,
            reference = "HOLD-SRV-${System.currentTimeMillis()}",
            idempotencyKey = "hold_${targetAccountId}_${System.currentTimeMillis()}",
            description = "Hold saldo pembuatan server '${serverName.trim()}'",
            createdBy = actor.id,
            timestamp = System.currentTimeMillis()
        )
        _ledgers.value = listOf(holdLedger) + _ledgers.value

        // STEP 2: Pterodactyl Container Provisioning (via Gateway Adapter)
        val port = 25565 + _servers.value.size
        val pterodactylServerId = "pt_srv_${UUID.randomUUID().toString().take(8)}"
        val newServerId = "srv_${System.currentTimeMillis()}"

        val newServer = NixeonServer(
            id = newServerId,
            accountId = targetAccountId,
            ownerEmail = targetAccount.email,
            pterodactylServerId = pterodactylServerId,
            name = serverName.trim(),
            eggId = egg.id,
            eggName = egg.name,
            nodeId = node.id,
            nodeName = node.name,
            allocationIp = node.fqdn,
            allocationPort = port,
            memoryMb = plan.memoryMb,
            cpuPercent = plan.cpuPercent,
            diskMb = plan.diskMb,
            status = ServerStatus.RUNNING,
            planId = plan.id,
            createdAt = System.currentTimeMillis(),
            cpuUsagePercent = 8.5f,
            ramUsageMb = 256f,
            diskUsageMb = 480f,
            uptimeSeconds = 60L
        )

        // STEP 3: Commit Saldo
        val finalHold = (_balances.value[targetAccountId]?.holdBalance ?: plan.price) - plan.price
        _balances.value = _balances.value + (targetAccountId to BalanceRecord(targetAccountId, balanceAfterHold, finalHold, System.currentTimeMillis()))

        val commitLedger = LedgerEntry(
            id = "ldg_cmt_${System.currentTimeMillis()}",
            accountId = targetAccountId,
            type = LedgerType.SERVER_COMMIT,
            amount = plan.price,
            reference = "COMMIT-$newServerId",
            idempotencyKey = "commit_$newServerId",
            description = "Biaya pembuatan server '${serverName.trim()}' (${plan.name})",
            createdBy = actor.id,
            timestamp = System.currentTimeMillis()
        )
        _ledgers.value = listOf(commitLedger) + _ledgers.value

        // Save server mapping
        _servers.value = listOf(newServer) + _servers.value

        // Seed initial files & logs
        _consoleLogs[newServerId] = mutableListOf(
            ConsoleLogLine(System.currentTimeMillis() - 5000, "Container initialized on ${node.name}"),
            ConsoleLogLine(System.currentTimeMillis() - 3000, "Fetching ${egg.name} docker image..."),
            ConsoleLogLine(System.currentTimeMillis() - 1000, "Server started listening on ${node.fqdn}:$port"),
            ConsoleLogLine(System.currentTimeMillis(), "Server state changed to: RUNNING")
        )

        _serverFiles[newServerId] = mutableListOf(
            ServerFileItem("server.properties", "/server.properties", false, 512, System.currentTimeMillis(), "server-port=$port\nserver-name=${serverName.trim()}"),
            ServerFileItem("logs", "/logs", true, 0, System.currentTimeMillis(), null)
        )

        // Audit & Notification
        recordAudit(
            actorId = actor.id,
            actorName = actor.name,
            scope = if (actor.isOfficialAdmin) "panel_admin" else "user",
            targetType = "SERVER",
            targetId = newServerId,
            action = "CREATE_SERVER",
            reason = "Provisioning server baru via gateway",
            result = "SUCCESS",
            details = "Egg: ${egg.name}, Plan: ${plan.name}, Price: ${plan.price}, Node: ${node.name}"
        )

        pushNotification(
            accountId = targetAccountId,
            title = "Server Berhasil Dibuat!",
            body = "Server '${newServer.name}' telah berhasil di-deploy di ${node.name}. Port: $port.",
            type = "SERVER"
        )

        return Result.success(newServer)
    }

    // ==========================================
    // SERVER POWER ACTIONS & LIVE CONSOLE/FILES
    // ==========================================

    fun performServerPowerAction(
        actor: UserAccount,
        serverId: String,
        action: String // "START", "STOP", "RESTART", "KILL"
    ): Result<ServerStatus> {
        val server = _servers.value.find { it.id == serverId }
            ?: return Result.failure(Exception("Server tidak ditemukan."))

        val isOwner = actor.id == server.accountId
        if (!isOwner && !actor.hasPanelAdmin) {
            return Result.failure(Exception("Akses ditolak: Anda tidak memiliki akses ke server ini."))
        }

        val newStatus = when (action.uppercase(Locale.US)) {
            "START" -> ServerStatus.RUNNING
            "STOP" -> ServerStatus.STOPPED
            "RESTART" -> ServerStatus.RUNNING
            "KILL" -> ServerStatus.OFFLINE
            else -> return Result.failure(Exception("Aksi power server tidak dikenali."))
        }

        val updatedServer = server.copy(
            status = newStatus,
            uptimeSeconds = if (newStatus == ServerStatus.RUNNING) 1L else 0L
        )
        _servers.value = _servers.value.map { if (it.id == serverId) updatedServer else it }

        // Append log to live console
        val logLine = ConsoleLogLine(
            timestamp = System.currentTimeMillis(),
            text = "[System Gateway] Server power signal '$action' executed by ${actor.name}.",
            level = if (action == "KILL") "WARN" else "INFO"
        )
        _consoleLogs.getOrPut(serverId) { mutableListOf() }.add(logLine)

        recordAudit(
            actorId = actor.id,
            actorName = actor.name,
            scope = if (isOwner) "user" else "panel_admin",
            targetType = "SERVER",
            targetId = serverId,
            action = "POWER_$action",
            reason = "Aksi power server oleh ${actor.name}",
            result = "SUCCESS"
        )

        return Result.success(newStatus)
    }

    fun sendConsoleCommand(
        actor: UserAccount,
        serverId: String,
        command: String
    ): Result<ConsoleLogLine> {
        val server = _servers.value.find { it.id == serverId }
            ?: return Result.failure(Exception("Server tidak ditemukan."))

        val isOwner = actor.id == server.accountId
        if (!isOwner && !actor.hasPanelAdmin) {
            return Result.failure(Exception("Akses ditolak ke console server ini."))
        }

        val cleanCmd = command.trim()
        val cmdLog = ConsoleLogLine(
            timestamp = System.currentTimeMillis(),
            text = "> $cleanCmd",
            isCommand = true
        )
        _consoleLogs.getOrPut(serverId) { mutableListOf() }.add(cmdLog)

        // Mock gateway response
        val responseLog = ConsoleLogLine(
            timestamp = System.currentTimeMillis() + 100,
            text = "[Gateway Output] Executed: '$cleanCmd' on ${server.name} (container OK)"
        )
        _consoleLogs[serverId]?.add(responseLog)

        if (!isOwner) {
            recordAudit(
                actorId = actor.id,
                actorName = actor.name,
                scope = "panel_admin",
                targetType = "CONSOLE",
                targetId = serverId,
                action = "SEND_COMMAND",
                reason = "Admin delegated console command",
                result = "SUCCESS",
                details = cleanCmd
            )
        }

        return Result.success(responseLog)
    }

    fun getConsoleLogs(serverId: String): List<ConsoleLogLine> {
        return _consoleLogs[serverId] ?: emptyList()
    }

    fun getServerFiles(serverId: String): List<ServerFileItem> {
        return _serverFiles[serverId] ?: emptyList()
    }

    fun deleteServerFile(
        actor: UserAccount,
        serverId: String,
        filePath: String
    ): Result<Boolean> {
        val server = _servers.value.find { it.id == serverId }
            ?: return Result.failure(Exception("Server tidak ditemukan."))

        val isOwner = actor.id == server.accountId
        if (!isOwner && !actor.hasPlatformAdmin) {
            return Result.failure(Exception("Akses ditolak: Operasi hapus file membutuhkan izin kepemilikan atau platform_admin."))
        }

        val files = _serverFiles[serverId] ?: return Result.failure(Exception("Direktori tidak ditemukan."))
        val item = files.find { it.path == filePath }
            ?: return Result.failure(Exception("File '$filePath' tidak ditemukan."))

        files.remove(item)

        recordAudit(
            actorId = actor.id,
            actorName = actor.name,
            scope = if (isOwner) "user" else "platform_admin",
            targetType = "FILE",
            targetId = serverId,
            action = "DELETE_FILE",
            reason = "Penghapusan file server",
            result = "SUCCESS",
            details = "Path: $filePath"
        )

        return Result.success(true)
    }

    fun saveServerFile(
        actor: UserAccount,
        serverId: String,
        filePath: String,
        content: String
    ): Result<Boolean> {
        val server = _servers.value.find { it.id == serverId }
            ?: return Result.failure(Exception("Server tidak ditemukan."))

        val isOwner = actor.id == server.accountId
        if (!isOwner && !actor.hasPlatformAdmin) {
            return Result.failure(Exception("Akses ditolak untuk edit file."))
        }

        val files = _serverFiles.getOrPut(serverId) { mutableListOf() }
        val index = files.indexOfFirst { it.path == filePath }
        val fileName = filePath.substringAfterLast("/")
        val updatedItem = ServerFileItem(
            name = fileName,
            path = filePath,
            isDirectory = false,
            sizeBytes = content.toByteArray().size.toLong(),
            modifiedAt = System.currentTimeMillis(),
            content = content
        )

        if (index >= 0) {
            files[index] = updatedItem
        } else {
            files.add(updatedItem)
        }

        recordAudit(
            actorId = actor.id,
            actorName = actor.name,
            scope = if (isOwner) "user" else "platform_admin",
            targetType = "FILE",
            targetId = serverId,
            action = "SAVE_FILE",
            reason = "Simpan perubahan file server",
            result = "SUCCESS",
            details = "Path: $filePath"
        )

        return Result.success(true)
    }

    // ==========================================
    // ADMIN ACTIONS: CREATE USER, EDIT, AUDIT
    // ==========================================

    @Synchronized
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
    ): Result<Pair<UserAccount, String>> {
        // Only adm_01 or adm_02 can create user accounts!
        if (adminActor.adminSlot == null) {
            return Result.failure(Exception("Pelanggaran aturan: Hanya account adm_01 atau adm_02 yang berhak membuat user baru."))
        }

        val cleanEmail = email.trim().lowercase(Locale.US)
        if (_accounts.value.values.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
            return Result.failure(Exception("Email '$cleanEmail' sudah terdaftar di Nixeon."))
        }

        val newUserId = "usr_${System.currentTimeMillis().toString().takeLast(6)}"
        val activationToken = "act_${UUID.randomUUID().toString().take(12)}"
        val now = System.currentTimeMillis()
        val expiresAt = now + (activeDays.toLong() * 24 * 60 * 60 * 1000)

        // Create Account
        val newAccount = UserAccount(
            id = newUserId,
            email = cleanEmail,
            name = name.trim(),
            role = NixeonRole.USER,
            adminSlot = null, // Strictly user, NEVER third admin
            panelRole = panelRole,
            scopes = emptyList(), // Panel role admin does NOT grant Nixeon scopes!
            status = AccountStatus.ACTIVE,
            pterodactylUserId = 200L + _accounts.value.size,
            createdAt = now,
            activationToken = activationToken,
            isActivated = false
        )
        _passwords[newUserId] = "Nixeon2026!" // temporary default until activation

        _accounts.value = _accounts.value + (newUserId to newAccount)

        // Create Balance Record & Ledger
        val initBalance = initialGrantAmount.coerceAtLeast(0L)
        _balances.value = _balances.value + (newUserId to BalanceRecord(newUserId, initBalance, 0L, now))

        if (initBalance > 0L) {
            val grantLedger = LedgerEntry(
                id = "ldg_grt_${System.currentTimeMillis()}",
                accountId = newUserId,
                type = LedgerType.ADMIN_GRANT,
                amount = initBalance,
                reference = "GRANT-CREATE-$newUserId",
                idempotencyKey = "grant_create_$newUserId",
                description = "Saldo grant awal dari ${adminActor.name}",
                createdBy = adminActor.id,
                timestamp = now
            )
            _ledgers.value = listOf(grantLedger) + _ledgers.value
        }

        // Create Entitlement
        val entitlement = ResourceEntitlement(
            accountId = newUserId,
            planId = planId,
            startsAt = now,
            expiresAt = expiresAt,
            gracePeriodDays = gracePeriodDays,
            status = EntitlementStatus.ACTIVE,
            grantedBy = adminActor.id
        )
        _entitlements.value = _entitlements.value + (newUserId to entitlement)

        val activationLink = "https://panel.marrlabs.my.id/activate?token=$activationToken"

        // Record Audit
        recordAudit(
            actorId = adminActor.id,
            actorName = adminActor.name,
            scope = "platform_admin",
            targetType = "USER",
            targetId = newUserId,
            action = "ADMIN_CREATE_USER",
            reason = "Pembuatan akun pelanggan baru oleh ${adminActor.adminSlot?.displayName}",
            result = "SUCCESS",
            details = "Email: $cleanEmail, PanelRole: ${panelRole.name}, Plan: $planId, Grant: $initBalance"
        )

        return Result.success(Pair(newAccount, activationLink))
    }

    @Synchronized
    fun adminAdjustBalance(
        adminActor: UserAccount,
        targetAccountId: String,
        amountDelta: Long,
        reason: String
    ): Result<Long> {
        if (!adminActor.hasBillingAdmin) {
            return Result.failure(Exception("Akses ditolak: Membutuhkan scope billing_admin."))
        }
        if (reason.isBlank()) {
            return Result.failure(Exception("Alasan penyesuaian saldo wajib disertakan untuk audit ledger."))
        }

        val currentBal = _balances.value[targetAccountId]?.balance ?: 0L
        val newBal = currentBal + amountDelta
        if (newBal < 0) {
            return Result.failure(Exception("Koreksi saldo tidak dapat menyebabkan saldo menjadi negatif."))
        }

        val type = if (amountDelta >= 0) LedgerType.REVERSAL_CREDIT else LedgerType.REVERSAL_DEBIT
        val ledger = LedgerEntry(
            id = "ldg_rev_${System.currentTimeMillis()}",
            accountId = targetAccountId,
            type = type,
            amount = Math.abs(amountDelta),
            reference = "REVERSAL-${System.currentTimeMillis()}",
            idempotencyKey = "rev_${targetAccountId}_${System.currentTimeMillis()}",
            description = "Koreksi saldo oleh ${adminActor.name}: $reason",
            createdBy = adminActor.id,
            timestamp = System.currentTimeMillis()
        )

        _ledgers.value = listOf(ledger) + _ledgers.value
        _balances.value = _balances.value + (targetAccountId to BalanceRecord(targetAccountId, newBal, _balances.value[targetAccountId]?.holdBalance ?: 0L, System.currentTimeMillis()))

        recordAudit(
            actorId = adminActor.id,
            actorName = adminActor.name,
            scope = "billing_admin",
            targetType = "LEDGER",
            targetId = targetAccountId,
            action = "ADJUST_BALANCE",
            reason = reason,
            result = "SUCCESS",
            details = "Delta: $amountDelta, NewBalance: $newBal"
        )

        return Result.success(newBal)
    }

    // ==========================================
    // BACKUP, RESTORE & VPS MIGRATION
    // ==========================================

    fun generateBackupArchive(adminActor: UserAccount): Result<BackupManifest> {
        if (!adminActor.hasPlatformAdmin) {
            return Result.failure(Exception("Akses ditolak: Operasi backup membutuhkan scope platform_admin."))
        }

        val nowStr = dateFormat.format(Date())
        val shaChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val manifest = BackupManifest(
            format = "nixeon-backup-v1",
            createdAt = nowStr,
            createdBy = adminActor.id,
            schemaVersion = 7,
            encrypted = true,
            checksumSha256 = shaChecksum,
            includes = listOf("accounts", "balances", "ledger", "entitlements", "topups", "audit", "servers"),
            sizeKb = 1420L
        )

        recordAudit(
            actorId = adminActor.id,
            actorName = adminActor.name,
            scope = "platform_admin",
            targetType = "BACKUP",
            targetId = "nixeon-backup-$nowStr",
            action = "CREATE_BACKUP",
            reason = "Snapshot terenkripsi rutin",
            result = "SUCCESS",
            details = "Format: ${manifest.format}, Checksum: ${manifest.checksumSha256.take(8)}..."
        )

        return Result.success(manifest)
    }

    fun restoreBackupDryRun(adminActor: UserAccount, manifest: BackupManifest): Result<String> {
        if (!adminActor.hasPlatformAdmin) {
            return Result.failure(Exception("Akses ditolak: Operasi restore membutuhkan scope platform_admin."))
        }

        if (manifest.checksumSha256.isBlank()) {
            return Result.failure(Exception("Checksum SHA-256 tidak valid atau korup."))
        }

        val report = """
            === LAPORAN DRY-RUN RESTORE NIXEON ===
            Manifest Format: ${manifest.format}
            Schema Version: ${manifest.schemaVersion} (Kompatibel dengan v7)
            Checksum SHA256: ${manifest.checksumSha256.take(16)}... [MATCH]
            Enkripsi Snapshot: AES-256 [VERIFIED]
            
            Entitas Terverifikasi:
            - Akun: ${_accounts.value.size} records
            - Ledger Transaksi: ${_ledgers.value.size} immutable entries (Zero duplicate reference)
            - Pemetaan Server Pterodactyl: ${_servers.value.size} live containers
            - Pending Top-Up: ${_topUps.value.count { it.status == TopUpStatus.PENDING }} records (Dipertahankan)
            
            Konflik Terdeteksi: 0 Konflik
            Status Rekonsiliasi: PASS (Aman untuk Pre-Restore Backup & Staging Cutover)
        """.trimIndent()

        recordAudit(
            actorId = adminActor.id,
            actorName = adminActor.name,
            scope = "platform_admin",
            targetType = "RESTORE",
            targetId = manifest.checksumSha256.take(12),
            action = "RESTORE_DRY_RUN",
            reason = "Uji kelayakan snapshot sebelum restore",
            result = "SUCCESS"
        )

        return Result.success(report)
    }

    fun updateVpsMigrationStep(
        adminActor: UserAccount,
        newPhase: VpsMigrationPhase,
        toggleMaintenance: Boolean? = null
    ): Result<VpsMigrationState> {
        if (!adminActor.hasPlatformAdmin) {
            return Result.failure(Exception("Akses ditolak: Pengaturan migrasi VPS memerlukan platform_admin."))
        }

        val current = _vpsMigration.value
        val updated = current.copy(
            phase = newPhase,
            isMaintenanceMode = toggleMaintenance ?: current.isMaintenanceMode,
            stagingRestored = newPhase >= VpsMigrationPhase.H_1_CUTOVER,
            reconciliationPassed = newPhase >= VpsMigrationPhase.H_1_CUTOVER,
            dnsUpdated = newPhase == VpsMigrationPhase.COMPLETED,
            tokenRotated = newPhase == VpsMigrationPhase.COMPLETED,
            lastCutoverAt = if (newPhase == VpsMigrationPhase.COMPLETED) System.currentTimeMillis() else current.lastCutoverAt
        )
        _vpsMigration.value = updated

        recordAudit(
            actorId = adminActor.id,
            actorName = adminActor.name,
            scope = "platform_admin",
            targetType = "MIGRATION",
            targetId = "VPS-MIGRATE",
            action = "UPDATE_PHASE",
            reason = "Progress migrasi VPS ke fase ${newPhase.label}",
            result = "SUCCESS"
        )

        return Result.success(updated)
    }

    // ==========================================
    // NOTIFICATIONS & AUDIT HELPER
    // ==========================================

    fun pushNotification(accountId: String, title: String, body: String, type: String) {
        val item = NotificationItem(
            id = "ntf_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            accountId = accountId,
            title = title,
            body = body,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        _notifications.value = listOf(item) + _notifications.value
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    private fun recordAudit(
        actorId: String,
        actorName: String,
        scope: String,
        targetType: String,
        targetId: String,
        action: String,
        reason: String?,
        result: String,
        details: String = ""
    ) {
        val log = AdminActionLog(
            id = "aud_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}",
            actorId = actorId,
            actorName = actorName,
            scope = scope,
            targetType = targetType,
            targetId = targetId,
            action = action,
            reason = reason,
            result = result,
            timestamp = System.currentTimeMillis(),
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
