import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.lovemoney.lovemoney"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("../lovemoney-release.keystore")
            storePassword = "lovemoney2025"
            keyAlias = "lovemoney"
            keyPassword = "lovemoney2025"
        }
    }

    defaultConfig {
        applicationId = "com.lovemoney.lovemoney"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Set output APK name with version
        setProperty("archivesBaseName", "LoveMoney-v${versionName}")
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val dateFormat = SimpleDateFormat("yyyyMMdd")
            val date = dateFormat.format(Date())
            val versionName = variant.versionName
            val versionCode = variant.versionCode

            val fileName = when (variant.buildType.name) {
                "release" -> "LoveMoney-v${versionName}-${versionCode}-${date}-release.apk"
                "debug" -> "LoveMoney-v${versionName}-${versionCode}-${date}-debug.apk"
                else -> "LoveMoney-v${versionName}-${versionCode}-${date}-${variant.buildType.name}.apk"
            }

            output.outputFileName = fileName
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = false
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.webkit:webkit:1.10.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}