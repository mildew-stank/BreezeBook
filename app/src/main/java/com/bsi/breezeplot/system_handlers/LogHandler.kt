package com.bsi.breezeplot.system_handlers

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.bsi.breezeplot.DATE_FORMAT
import com.bsi.breezeplot.TIME_FORMAT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

@Entity(tableName = "log_entries")
data class LogEntry(
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val bearing: Float,
    val time: String,
    val date: String,
    val distance: Float = 0f,
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
}

@Database(entities = [LogEntry::class], version = 1, exportSchema = false) // Increment version on schema changes
abstract class AppDatabase : RoomDatabase() {
    abstract fun logEntryDao(): LogEntryDao

    companion object {
        @Volatile // Ensures visibility of INSTANCE across threads
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // synchronized block for thread safety
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "log_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val logRepository: LogRepository
    val persistedLogEntries: StateFlow<List<LogEntry>>

    init {
        val logEntryDao = AppDatabase.getDatabase(application).logEntryDao()
        logRepository = LogRepository(logEntryDao)
        persistedLogEntries = logRepository.allLogEntries
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // Keep subscribed for 5s after last collector
                initialValue = emptyList()
            )
    }

    fun addLogEntry(
        latitudeVal: Double,
        longitudeVal: Double,
        speedVal: Float,
        bearingVal: Float,
        distanceVal: Float = 0f
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = ZonedDateTime.now(ZoneOffset.UTC)
            val newEntry = LogEntry(
                latitude = latitudeVal,
                longitude = longitudeVal,
                speed = speedVal,
                bearing = bearingVal,
                distance = distanceVal,
                time = currentTime.format(TIME_FORMAT),
                date = currentTime.format(DATE_FORMAT),
            )
            logRepository.insert(newEntry)
        }
    }

    fun deleteLogEntry(logEntry: LogEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            logRepository.delete(logEntry)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            logRepository.clearAll()
        }
    }

    suspend fun logToCSV(): String {
        val logEntries = logRepository.allLogEntries.first()
        if (logEntries.isEmpty()) {
            return ""
        }
        val stringBuilder = StringBuilder()

        stringBuilder.append("latitude,longitude,speed,bearing,time,date,trip\n")
        logEntries.forEach { entry ->
            stringBuilder.append("${entry.latitude},${entry.longitude},${entry.speed},${entry.bearing},\"${entry.time}\",\"${entry.date}\",${entry.distance}\n")
        }
        return stringBuilder.toString()
    }

    suspend fun tryWriteCsvToUri(uri: Uri, csvData: String, context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(csvData)
                    }
                }
                true
            } catch (error: IOException) {
                error.printStackTrace()
                false
            }
        }
    }
}
