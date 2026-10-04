import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Questlog — app module build script (single-module project).
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Secrets live in local.properties (git-ignored) and are surfaced to code via BuildConfig.
// Mirrors how the Android Gradle Plugin itself reads sdk.dir.
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val rawgApiKey: String = localProperties.getProperty("RAWG_API_KEY") ?: ""
val oneSignalAppId: String = localProperties.getProperty("ONESIGNAL_APP_ID") ?: ""

android {
    namespace = "com.questlog.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.questlog.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API keys — read from local.properties, never hardcoded.
        buildConfigField("String", "RAWG_API_KEY", "\"$rawgApiKey\"")
        buildConfigField("String", "ONESIGNAL_APP_ID", "\"$oneSignalAppId\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Room emits its JSON schema on every build; keep it in the module so migrations are reviewable.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // --- Compose BOM: pins every androidx.compose.* version below ---
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // --- Compose UI / Material 3 ---
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- AndroidX core + activity ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Navigation Compose ---
    implementation(libs.androidx.navigation.compose)

    // --- Lifecycle (ViewModel + runtime-compose for collectAsStateWithLifecycle) ---
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // --- kotlinx.datetime ---
    implementation(libs.kotlinx.datetime)

    // --- DataStore (preferences) ---
    implementation(libs.androidx.datastore.preferences)

    // --- Room ---
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // --- Hilt (KSP processor) ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Retrofit + Gson + OkHttp logging ---
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    // --- Coil 3 (image loading; okhttp engine for network images) ---
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // --- ML Kit on-device text recognition ---
    implementation(libs.mlkit.text.recognition)

    // --- OneSignal (push notifications) ---
    implementation(libs.onesignal)

    // --- Solana (on-chain proof) ---
    implementation(libs.solana.web3.solana)
    implementation(libs.solana.rpc.core)
    implementation(libs.solana.mobile.wallet.adapter.clientlib.ktx)
    implementation(libs.multimult)

    // --- Tests ---
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.org.json)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.room.testing)
}
