plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "su.gwarden.music"
    compileSdk = 35

    defaultConfig {
        applicationId = "su.gwarden.music"
        minSdk = 24
        targetSdk = 35
        versionCode = 243
        versionName = "12.50"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
        }
        debug {
            isMinifyEnabled = false
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
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // ExoPlayer вместо системного MediaPlayer: у Samsung MediaPlayer падал
    // с -38 (ENOSYS) и на потоке, и на локальном файле, причём волной -
    // после первого сбоя не играло вообще ничего
    implementation("androidx.media3:media3-exoplayer:1.4.1")
}
