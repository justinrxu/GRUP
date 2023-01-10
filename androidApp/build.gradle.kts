val realmVersion: String by project
plugins {
    id("com.android.application")
    kotlin("android")
    id("io.realm.kotlin")
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "com.grup.android"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    implementation(project(":shared"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")

    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    // Realm
    implementation("io.realm.kotlin:library-base:$realmVersion")

    // Jetpack Compose
    val composeVersion = "1.0.5"
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.ui:ui-tooling:1.0.0-alpha07")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.navigation:navigation-compose:2.4.1")

    // Splash API
    implementation("androidx.core:core-splashscreen:1.0.0-beta01")

    val accompanist_version = "0.15.0"
    // Pager and Indicators - Accompanist
    implementation ("com.google.accompanist:accompanist-pager:$accompanist_version")
    implementation ("com.google.accompanist:accompanist-pager-indicators:$accompanist_version")
}