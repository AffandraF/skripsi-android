plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

val WEB_CLIENT_ID: String by project

android {
    namespace = "com.example.skripsi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.skripsi"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "WEB_CLIENT_ID", "\"${WEB_CLIENT_ID}\"")
        }
        debug {
            buildConfigField("String", "WEB_CLIENT_ID", "\"${WEB_CLIENT_ID}\"")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.v1130)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.core.splashscreen)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.credentials.v130)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.core.ktx)
    implementation(libs.guava)
    implementation(libs.glide)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}