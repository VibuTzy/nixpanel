package com.example.nixeon.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_servers")
data class CachedServerEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val name: String,
    val eggName: String,
    val allocation: String,
    val memoryMb: Int,
    val cpuPercent: Int,
    val diskMb: Int,
    val status: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_balance")
data class CachedBalanceEntity(
    @PrimaryKey val accountId: String,
    val balance: Long,
    val holdBalance: Long,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_notifications")
data class CachedNotificationEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val title: String,
    val body: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean
)

@Entity(tableName = "cached_ledgers")
data class CachedLedgerEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val type: String,
    val amount: Long,
    val reference: String,
    val description: String,
    val timestamp: Long
)

@Dao
interface CachedServerDao {
    @Query("SELECT * FROM cached_servers WHERE accountId = :accountId ORDER BY lastUpdated DESC")
    fun getServersForAccount(accountId: String): Flow<List<CachedServerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<CachedServerEntity>)

    @Query("DELETE FROM cached_servers WHERE accountId = :accountId")
    suspend fun clearServersForAccount(accountId: String)
}

@Dao
interface CachedBalanceDao {
    @Query("SELECT * FROM cached_balance WHERE accountId = :accountId LIMIT 1")
    fun getBalance(accountId: String): Flow<CachedBalanceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBalance(balance: CachedBalanceEntity)
}

@Dao
interface CachedNotificationDao {
    @Query("SELECT * FROM cached_notifications WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getNotifications(accountId: String): Flow<List<CachedNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<CachedNotificationEntity>)

    @Query("UPDATE cached_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}

@Dao
interface CachedLedgerDao {
    @Query("SELECT * FROM cached_ledgers WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getLedgers(accountId: String): Flow<List<CachedLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgers(ledgers: List<CachedLedgerEntity>)
}

@Database(
    entities = [
        CachedServerEntity::class,
        CachedBalanceEntity::class,
        CachedNotificationEntity::class,
        CachedLedgerEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NixeonDatabase : RoomDatabase() {
    abstract fun serverDao(): CachedServerDao
    abstract fun balanceDao(): CachedBalanceDao
    abstract fun notificationDao(): CachedNotificationDao
    abstract fun ledgerDao(): CachedLedgerDao
}
