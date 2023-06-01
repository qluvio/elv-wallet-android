@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.sqldelight)
    kotlin("kapt")
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
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.rxjava3)


    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation (libs.retrofit)
    implementation (libs.retrofit.moshi)
    implementation (libs.retrofit.rxjava)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation (libs.rxandroid)
    // Because RxAndroid releases are few and far between, it is recommended you also
    // explicitly depend on RxJava's latest version for bug fixes and new features.
    // (see https://github.com/ReactiveX/RxJava/releases for latest 3.x.x version)
    implementation (libs.rxjava)
    implementation(libs.rxkotlin)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.navigation.compose)

    implementation(libs.qrcode.kotlin.android)

    implementation (libs.androidx.lifecycle.process)

    implementation(libs.sqldelight.androidDriver)
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

sqldelight {
    databases {
        create("WalletDatabase") {
            packageName.set("app.eluvio.wallet")
        }
    }
}
