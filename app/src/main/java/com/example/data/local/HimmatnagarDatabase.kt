package com.example.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Database entities & DAOs for local storage caching according to room-database-integration skill.
 */
@Entity(tableName = "local_cache_entries")
data class CacheEntry(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface CacheDao {
    @Query("SELECT value FROM local_cache_entries WHERE `key` = :key LIMIT 1")
    fun observeValue(key: String): Flow<String?>

    @Query("SELECT value FROM local_cache_entries WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: CacheEntry)

    @Query("DELETE FROM local_cache_entries WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM local_cache_entries")
    suspend fun clear()
}

@Database(entities = [CacheEntry::class], version = 1, exportSchema = false)
abstract class HimmatnagarDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile
        private var INSTANCE: HimmatnagarDatabase? = null

        fun getInstance(context: Context): HimmatnagarDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HimmatnagarDatabase::class.java,
                    "sheher_himmatnagar.db"
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
