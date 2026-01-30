plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.notes"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.notes"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "G_API_KEY", "\"API_KEY\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}
    dependencies {

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.firebase.crashlytics.buildtools)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
        implementation("com.google.ai.client.generativeai:generativeai:0.5.0")
        implementation("com.google.guava:guava:32.1.3-android")
        implementation("org.json:json:20231013")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")

    }

