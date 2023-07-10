@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    kotlin("kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.realm)
    alias(libs.plugins.gradle.secrets)
}

android {
    namespace = "app.eluvio.wallet"
    compileSdk = 33

    defaultConfig {
        applicationId = "app.eluvio.wallet"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.leanback)

    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.rxjava3)

    implementation(libs.compose.destinations)
    ksp(libs.compose.destinations.ksp)

    implementation(libs.coil)

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.retrofit.rxjava)
    implementation(libs.okhttp.logginginterceptor)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.rxandroid)
    // Because RxAndroid releases are few and far between, it is recommended you also
    // explicitly depend on RxJava's latest version for bug fixes and new features.
    // (see https://github.com/ReactiveX/RxJava/releases for latest 3.x.x version)
    implementation(libs.rxjava)
    implementation(libs.rxkotlin)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.navigation.compose)

    implementation(libs.qrcode.kotlin.android)

    implementation(libs.androidx.lifecycle.process)

    implementation(libs.realm)
    implementation(libs.kotlinx.coroutines.rx3)

    ksp(libs.moshi.codegen)
    implementation(libs.moshi.kotlin)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.dash)
    implementation(libs.androidx.media3.hls)
    implementation(libs.androidx.media3.okhttp)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.ui.leanback)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

secrets {
    propertiesFileName = "secrets.properties"
}
