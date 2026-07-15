plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nuxnamdeep"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nuxnamdeep"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ============================================
    // Android Core
    // ============================================
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ============================================
    // JSON (para leer archivos NAM)
    // ============================================
    implementation("com.google.code.gson:gson:2.11.0")

    // ============================================
    // Corrutinas (para operaciones en segundo plano)
    // ============================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ============================================
    // Permisos (para USB y almacenamiento)
    // ============================================
    implementation("com.guolindev.permissionx:permissionx:1.7.1")

    // ============================================
    // MIDI (android.media.midi ya está en el SDK)
    // ============================================

    // ============================================
    // Pruebas (opcional)
    // ============================================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
