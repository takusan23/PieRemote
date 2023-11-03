package io.github.takusan23.pieremote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object DataStore {

    /** サービスを利用するか */
    val IS_SERVICE_ENABLE = booleanPreferencesKey("is_service_enable")

}