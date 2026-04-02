package com.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.domain.repository.ChatLimitStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("chat_limit")

class ChatLimitDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : ChatLimitStore {

    companion object {
        val COUNT = intPreferencesKey("count")
        val DATE = stringPreferencesKey("date")
    }

    override val countFlow: Flow<Int> = context.dataStore.data.map {
        it[COUNT] ?: 0
    }

    override suspend fun increase(today: String) {
        context.dataStore.edit {
            val savedDate = it[DATE]

            if (savedDate != today) {
                it[COUNT] = 1
                it[DATE] = today
            } else {
                it[COUNT] = (it[COUNT] ?: 0) + 1
            }
        }
    }
}