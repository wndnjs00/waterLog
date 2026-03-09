package com.app.waterlog.di

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kakao.sdk.common.KakaoSdk
import com.app.waterlog.BuildConfig
import com.navercorp.nid.NidOAuth
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {
    
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        NidOAuth.initialize(
            context = this,
            clientId = BuildConfig.NAVER_CLIENT_ID,
            clientSecret = BuildConfig.NAVER_CLIENT_SECRET,
            clientName = "WaterLog",
        )
    }
}