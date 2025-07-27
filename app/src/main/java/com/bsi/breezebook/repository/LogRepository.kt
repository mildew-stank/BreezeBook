package com.bsi.breezebook.repository

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

@Entity(tableName = "log_entries")
data class LogEntry(
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val bearing: Float,
    val time: String,
    val date: String,
    var distance: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
)

@Dao
interface LogEntryDao {
    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllLogEntries(): Flow<List<LogEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(logEntry: LogEntry)

    @Delete
    suspend fun deleteLogEntry(logEntry: LogEntry)

    @Query("DELETE FROM log_entries")
    suspend fun clearAllLogEntries()

    @Query("SELECT * FROM log_entries WHERE id = :id")
    suspend fun getLogEntryById(id: String): LogEntry?
}

@Database(entities = [LogEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logEntryDao(): LogEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "log_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
class LogRepository(private val logEntryDao: LogEntryDao) {
    val allLogEntries: Flow<List<LogEntry>> = logEntryDao.getAllLogEntries()

    suspend fun insert(logEntry: LogEntry) {
        logEntryDao.insertLogEntry(logEntry)
    }

    suspend fun delete(logEntry: LogEntry) {
        logEntryDao.deleteLogEntry(logEntry)
    }

    suspend fun clearAll() {
        logEntryDao.clearAllLogEntries()
    }

    suspend fun getLogEntryById(id: String): LogEntry? {
        return logEntryDao.getLogEntryById(id)
    }

    suspend fun logToCsv(): String {
        return withContext(Dispatchers.IO) {
            val logEntries = allLogEntries.first()
            val stringBuilder = StringBuilder()

            stringBuilder.append("latitude,longitude,speed,bearing,time,date,trip\n") // Header
            logEntries.forEach { entry ->
                stringBuilder.append("${entry.latitude},${entry.longitude},${entry.speed},${entry.bearing},\"${entry.time}\",\"${entry.date}\",${entry.distance}\n")
            }
            stringBuilder.toString()
        }
    }
}
