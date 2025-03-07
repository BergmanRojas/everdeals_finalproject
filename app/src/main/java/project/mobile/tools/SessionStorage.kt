package project.mobile.tools

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Extensión para obtener el DataStore en un Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class SessionStorage(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    // Flujo para observar el estado de login
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_LOGGED_IN] ?: false }

    // Establecer el estado de login
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    // Obtener el estado de login de forma síncrona (usar con precaución)
    fun isUserLoggedIn(): Boolean = runBlocking {
        context.dataStore.data.first()[IS_LOGGED_IN] ?: false
    }
}
