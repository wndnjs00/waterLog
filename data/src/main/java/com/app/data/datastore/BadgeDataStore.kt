package com.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.domain.repository.BadgeShownStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private val Context.badgeDataStore by preferencesDataStore("badge_prefs")

class BadgeDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : BadgeShownStore {

    companion object {
        private val SHOWN_BADGES_KEY = stringSetPreferencesKey("shown_badges")
    }

    // 이미 본 뱃지 가져오기
    override val shownBadges: Flow<Set<String>> = context.badgeDataStore.data
        .map { preferences ->
            preferences[SHOWN_BADGES_KEY] ?: emptySet()
        }

    // 뱃지 저장
    override suspend fun saveBadge(key: String) {
        context.badgeDataStore.edit { preferences ->
            val current = preferences[SHOWN_BADGES_KEY] ?: emptySet()
            preferences[SHOWN_BADGES_KEY] = current + key
        }
    }
}