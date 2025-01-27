package ru.kts.panelcontrolc65

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "control_panel_prefs")

class OrderPreferencesManager(private val context: Context) {
    private val orderKey = stringPreferencesKey("control_buttons")
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun saveOrder(order: List<String>) {

        context.dataStore.edit { preferences ->
            preferences[orderKey] = order.joinToString("|")
        }
    }

    suspend fun getOrder(): Flow<List<String>> {
        return context.dataStore.data
            .map { preferences ->
                preferences[orderKey]?.split("|") ?: emptyList()
            }
    }

    init {
        scope.launch {
            if (getOrder().first().isEmpty()){ saveOrder(ControlButtons.entries.map { it.name }) }
        }
    }
}