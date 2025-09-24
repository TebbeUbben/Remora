package de.tebbeubben.remora.lib.persistence.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import de.tebbeubben.remora.lib.di.ApplicationContext
import de.tebbeubben.remora.lib.model.commands.RemoraCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CommandRepository @Inject constructor(
    @param:ApplicationContext
    private val appContext: Context,
) {

    private val prefs: SharedPreferences = appContext.getSharedPreferences("remora_command_repository", Context.MODE_PRIVATE)

    private object K {

        const val STATE = "state"
        const val LAST_SEQUENCE_ID = "last_sequence_id"
    }

    private val stateMutex = Mutex()
    private val _state = MutableStateFlow<RemoraCommand?>(null)
    private var loaded = false

    val stateFlow = _state.onStart { if(!loaded) { stateMutex.withLock { load() } } }

    private suspend fun load() = withContext(Dispatchers.IO) {
        if (!loaded) {
            prefs.getString(K.STATE, null)?.let { str ->
                _state.emit(Json.decodeFromString<RemoraCommand>(str))
            }
            loaded = true
        }
        _state.value
    }

    suspend fun save(command: RemoraCommand?) = stateMutex.withLock {
        Log.d("CommandRepository", "save command: $command")
        withContext(Dispatchers.IO) {
            if (command == null) {
                prefs.edit(true) { remove(K.STATE) }
                _state.value = null
            } else {
                prefs.edit(true) {
                    putString(K.STATE, Json.encodeToString<RemoraCommand>(command))
                }
                _state.value = command
            }
            loaded = true
        }
    }

    suspend fun getCached() = stateMutex.withLock { load() }

    suspend fun getNewSequenceId() = stateMutex.withLock {
        withContext(Dispatchers.IO) {
            val newId = (prefs.getInt(K.LAST_SEQUENCE_ID, 0) + 1).also {
                prefs.edit(true) { putInt(K.LAST_SEQUENCE_ID, it) }
            }
            newId
        }
    }
}