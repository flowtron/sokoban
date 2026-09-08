import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.devtools.ksp)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    namespace = "de.flowtron.sokoban"
    compileSdk = 37

    // TODO: proper versioning .. for now we do it manually
    // manual versioning
    val versionBase = "1.0.0" // base version we are working on
    val versionCode = 8 // build counter

    defaultConfig {
        applicationId = "de.flowtron.sokoban"
        minSdk = 34
        //noinspection OldTargetApi
        targetSdk = 35

        //versionCode = 8//versionCode
        versionName = "${versionBase}-alpha-${versionCode}" // e.g. "1.0.0-alpha-8"

        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        buildConfigField("int", "VERSION_CODE", versionCode.toString())
        buildConfigField("String", "BUILD_DATE", "\"${System.currentTimeMillis()}\"")
        buildConfigField("String", "BUILD_TYPE", "\"DEVELOPMENT\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val versionName = "${versionBase}-${versionCode}" // e.g "1.0.0-8"
            buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
            buildConfigField("String", "BUILD_TYPE", "\"RELEASE\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt)
    implementation(libs.androidx.compose.ui.unit)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.navigation.compose)
    implementation(libs.material.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}