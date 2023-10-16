val koinVersion: String by project
val kotlinVersion: String by project
val composeVersion: String by project
val lifecycleVersion: String by project
val firebaseBOMVersion: String by project
val kotlinExtensionVersion: String by project

val keystorePassword: String by project

plugins {
    id("com.android.application")
    kotlin("android")
    id("io.realm.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("/Users/justinxu/Documents/keystore/signedkey")
            storePassword = keystorePassword
            keyAlias = "upload"
            keyPassword = keystorePassword
        }
    }
    compileSdk = 34
    defaultConfig {
        applicationId = "com.grup.android"
        minSdk = 21
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
        signingConfig = signingConfigs.getByName("release")
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = kotlinExtensionVersion
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
    namespace = "com.grup.android"
}

dependencies {
    // Shared
    implementation(project(":shared"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("androidx.appcompat:appcompat:1.6.1")
    //noinspection GradleDependency
    implementation("androidx.core:core-ktx:1.10.1")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:$composeVersion")
    //noinspection GradleDependency
    implementation("androidx.activity:activity-compose:1.7.2")
    //noinspection GradleDependency
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")

    // Koin
    implementation("io.insert-koin:koin-android:$koinVersion")

    // Google Play Services
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-base:18.2.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:$firebaseBOMVersion"))

    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging-ktx")
}
