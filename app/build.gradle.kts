
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // id("kotlin-android-extensions") not support now
    id("kotlin-parcelize")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.time"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.time"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "1.8"
//        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//            pickFirsts.add("lib/x86/libc++_shared.so")
//            pickFirsts.add("lib/x86_64/libc++_shared.so")
//            pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
//            pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        }
    }
    buildFeatures{
        viewBinding = true
    }
    viewBinding{
        enable = true
    }
    configurations { implementation{
        exclude(group="org.jetbrains", module="annotations")
    } }
}

dependencies {
    val roomVersion = "2.1.0"
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("com.google.android.material:material:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.0.0")
    implementation("com.squareup.retrofit2:retrofit:2.6.1")
    implementation("com.squareup.retrofit2:converter-gson:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1")
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.0-alpha05")
    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.2.0")
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.2.0")
//    implementation("androidx.room:room-ktx:$roomVersion")
//    ksp("androidx.room:room-compiler:$room_version")
//    ksp("androidx.room:room-compiler:2.6.1")
}

//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.5.30")
//    }
//}


//ksp("androidx.room:room-compiler:$room_version")


kapt {
    correctErrorTypes=true
}


//
//dependencies {
//    ...
//    implementation "androidx.room:room-runtime:2.1.0"
//    kapt "androidx.room:room-compiler:2.1.0"
//}