plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services) // Firebase
    alias(libs.plugins.kotlin.kapt)                // Room
}

android {
    namespace   = "com.huertaonline.app"
    compileSdk  = 35

    defaultConfig {
        applicationId         = "com.huertaonline.app"
        minSdk                = 26
        targetSdk             = 35
        versionCode           = 1
        versionName           = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true   // ← Activa ViewBinding para todos los layouts XML
    }
}

dependencies {

    // ── AndroidX base ─────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // ── Firebase (BOM gestiona las versiones internamente) ─────────────
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // ── Google Sign-In ─────────────────────────────────────────────────
    implementation(libs.play.services.auth)

    // ── Room (base de datos local para el carrito) ─────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)           // kapt genera el código de Room

    // ── Lifecycle: ViewModel y LiveData ───────────────────────────────
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    // ── Navigation Component ──────────────────────────────────────────
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // ── Corrutinas ────────────────────────────────────────────────────
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)  // Para .await() con Firebase

    // ── Glide (carga imágenes desde URLs de Firebase Storage) ─────────
    implementation(libs.glide)

    // ── Tests ─────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}