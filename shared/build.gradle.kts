val ktorVersion: String by project
val realmVersion: String by project
val koinVersion: String by project
val awsVersion: String by project
val napierVersion = "2.4.0"

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.10"
    kotlin("native.cocoapods")
    id("com.android.library")
    id("io.realm.kotlin")
}

// CocoaPods requires the podspec to have a version.
version = "1.0"
kotlin {
    android()
    iosX64()
    iosArm64()
    //iosSimulatorArm64() sure all ios dependencies support this target

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            // Mandatory properties
            // Configure fields required by CocoaPods.
            summary = "Some description for a Kotlin/Native module"
            homepage = "Link to a Kotlin/Native module homepage"
            // Framework name configuration. Use this property instead of deprecated 'frameworkName'
            baseName = "shared"
        }

        // Maps custom Xcode configuration to NativeBuildType
        xcodeConfigurationToNativeBuildType["CUSTOM_DEBUG"] = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG
        xcodeConfigurationToNativeBuildType["CUSTOM_RELEASE"] = org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Kotlin Libraries
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                // Logger
                implementation("io.github.aakira:napier:$napierVersion")

                // Realm
                implementation("io.realm.kotlin:library-base:$realmVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

                // Realm Sync
                implementation("io.realm.kotlin:library-sync:$realmVersion")

                // Koin
                implementation("io.insert-koin:koin-core:$koinVersion")

                // Ktor
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                // Koin Test
                implementation("io.insert-koin:koin-test:$koinVersion")
            }
        }
        val androidMain by getting {
            dependencies {
                // Datastore
                implementation("androidx.datastore:datastore-preferences:1.0.0")

                // Ktor Client
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

                // AWS
                implementation("aws.sdk.kotlin:s3:$awsVersion")

                // Import the Firebase BoM
                implementation(platform("com.google.firebase:firebase-bom:31.2.2"))

                // Firebase Crashlytics
                implementation("com.google.firebase:firebase-analytics-ktx")
                implementation("com.google.firebase:firebase-crashlytics-ktx")

                // Firebase Cloud Messaging
                implementation("com.google.firebase:firebase-messaging-ktx")

            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        //val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            //iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        //val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            //iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }
}