plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "com.zhuinden.simplestackexamplescoping"
        minSdkVersion(14)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    // See https://kotlinlang.org/docs/reference/using-gradle.html#android-studio
    sourceSets["main"].java.srcDir("src/main/kotlin")
}

androidExtensions {
    isExperimental = true
}

dependencies {
    //implementation(mapOf("dir" to "libs", "include" to listOf("*.jar")))
    implementation(project(":simple-stack"))

    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.1") {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    implementation("com.android.support:appcompat-v7:28.0.0")
    implementation("com.android.support:design:28.0.0")
    testImplementation("junit:junit:4.12")

    implementation("io.reactivex.rxjava2:rxjava:2.1.16")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.2")
    implementation("com.jakewharton.rxbinding2:rxbinding:2.1.1")
    implementation("com.jakewharton.rxrelay2:rxrelay:2.0.0")

    implementation("android.arch.lifecycle:runtime:1.1.1") // not necessary if you are using Support Library 26.1+
    implementation("android.arch.lifecycle:extensions:1.1.1")

    implementation("com.github.Zhuinden:command-queue:0.0.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.31")
}
