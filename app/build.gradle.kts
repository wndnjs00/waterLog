import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    //Hilt
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    //firebase google plugin
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val kakaoKey = localProperties.getProperty("kakao_native_app_key")
    ?: throw GradleException("kakao_native_app_key not found in local.properties")
val naverClientId = localProperties.getProperty("naver_client_id")
    ?: throw GradleException("naver_client_id not found in local.properties")
val naverClientSecret = localProperties.getProperty("naver_client_secret")
    ?: throw GradleException("naver_client_secret not found in local.properties")

android {
    namespace = "com.app.waterlog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.waterlog"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["kakao_native_app_key"] = kakaoKey

        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKey\"")
        buildConfigField("String", "NAVER_CLIENT_ID", "\"$naverClientId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$naverClientSecret\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // firebase 연결
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-analytics")

    // firebase auth (추가 안해도되는듯 - bom이 있어서)
//    implementation("com.google.firebase:firebase-auth:24.0.1")
//    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // google 로그인
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // kakao 로그인
    implementation("com.kakao.sdk:v2-user:2.23.2")

    // firestore firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    //ThreeTenABP 라이브러리 (시간변환 라이브러리)
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

    // firebase functions
    implementation("com.google.firebase:firebase-functions-ktx")

    // Naver SDK
    implementation("com.navercorp.nid:oauth:5.11.2")

    // Firebase messaging (BOM이 버전 관리)
    implementation(libs.firebase.messaging.ktx)

    // splashscreen
    implementation(libs.androidx.core.splashscreen)
}