package com.my.calculator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calculator_settings")

data class CalculatorSettings(
    val calcMode: String = "COMP",
    val angleMode: String = "Deg",
    val roundingMode: String = "Norm_1",
    val turnOffClose: Boolean = false,
    val preferDecimal: Boolean = false,
    val decimalFormat: String = "locale"
)

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_CALC_MODE = stringPreferencesKey("calc_mode")
        val KEY_ANGLE_MODE = stringPreferencesKey("angle_mode")
        val KEY_ROUNDING_MODE = stringPreferencesKey("rounding_mode")
        val KEY_TURN_OFF_CLOSE = booleanPreferencesKey("turn_off_close")
        val KEY_PREFER_DECIMAL = booleanPreferencesKey("prefer_decimal")
        val KEY_DECIMAL_FORMAT = stringPreferencesKey("decimal_format")
    }

    val settingsFlow: Flow<CalculatorSettings> = context.dataStore.data.map { pref ->
        CalculatorSettings(
            calcMode = pref[KEY_CALC_MODE] ?: "COMP",
            angleMode = pref[KEY_ANGLE_MODE] ?: "Deg",
            roundingMode = pref[KEY_ROUNDING_MODE] ?: "Norm_1",
            turnOffClose = pref[KEY_TURN_OFF_CLOSE] ?: false,
            preferDecimal = pref[KEY_PREFER_DECIMAL] ?: false,
            decimalFormat = pref[KEY_DECIMAL_FORMAT] ?: "locale"
        )
    }

    suspend fun setCalcMode(mode: String) {
        context.dataStore.edit { it[KEY_CALC_MODE] = mode }
    }

    suspend fun setAngleMode(mode: String) {
        context.dataStore.edit { it[KEY_ANGLE_MODE] = mode }
    }

    suspend fun setRoundingMode(mode: String) {
        context.dataStore.edit { it[KEY_ROUNDING_MODE] = mode }
    }

    suspend fun setTurnOffClose(value: Boolean) {
        context.dataStore.edit { it[KEY_TURN_OFF_CLOSE] = value }
    }

    suspend fun setPreferDecimal(value: Boolean) {
        context.dataStore.edit { it[KEY_PREFER_DECIMAL] = value }
    }

    suspend fun setDecimalFormat(format: String) {
        context.dataStore.edit { it[KEY_DECIMAL_FORMAT] = format }
    }
}
