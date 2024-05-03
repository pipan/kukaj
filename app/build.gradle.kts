plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "gaspapp.kukaj"
    compileSdk = 34

    defaultConfig {
        applicationId = "gaspapp.kukaj"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "1.0.4"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
}