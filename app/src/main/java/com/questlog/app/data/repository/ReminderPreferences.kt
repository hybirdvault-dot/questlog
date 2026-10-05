package com.questlog.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reminderDataStore by preferencesDataStore(name = "reminder")

@Singleton
class ReminderPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val enabled: Flow<Boolean> = context.reminderDataStore.data.map { it[ENABLED] ?: true }

    val noNag: Flow<Boolean> = context.reminderDataStore.data.map { it[NO_NAG] ?: false }

    val askedForReminder: Flow<Boolean> =
        context.reminderDataStore.data.map { it[ASKED] ?: false }

    val lastNaggedEpochDay: Flow<Long> =
        context.reminderDataStore.data.map { it[LAST_NAGGED] ?: 0L }

    suspend fun setEnabled(value: Boolean) {
        context.reminderDataStore.edit { it[ENABLED] = value }
    }

    suspend fun setNoNag(value: Boolean) {
        context.reminderDataStore.edit { it[NO_NAG] = value }
    }

    suspend fun setAskedForReminder(value: Boolean) {
        context.reminderDataStore.edit { it[ASKED] = value }
    }

    suspend fun markNagged(epochDay: Long) {
        context.reminderDataStore.edit { it[LAST_NAGGED] = epochDay }
    }

    private companion object {
        val ENABLED = booleanPreferencesKey("reminder_enabled")
        val NO_NAG = booleanPreferencesKey("reminder_no_nag")
        val ASKED = booleanPreferencesKey("reminder_asked")
        val LAST_NAGGED = longPreferencesKey("reminder_last_nagged_epoch_day")
    }
}
