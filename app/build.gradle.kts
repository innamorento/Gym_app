plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.gym.tracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gym.tracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

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
        compose = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":feature:templates"))
    implementation(project(":feature:exercises"))
    implementation(project(":feature:workout"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // WorkManager (Sprint 17 Reminders)
    implementation(libs.androidx.work.runtime.ktx)
}

// [START:TASK-020] Unified root APK deploy automation (untracked for configuration cache)
abstract class CopyApkToRootTask : DefaultTask() {
    init {
        doNotTrackState("Copies APK directly to root without tracking")
    }

    @get:Internal
    abstract val apkFile: RegularFileProperty

    @get:Internal
    abstract val targetApk: RegularFileProperty

    @TaskAction
    fun copyApk() {
        val src = apkFile.get().asFile
        val dest = targetApk.get().asFile
        if (src.exists()) {
            src.copyTo(dest, overwrite = true)
            println("APK successfully copied to root: ${dest.name} (${dest.length()} bytes)")
        }
    }
}

val copyApkTask = tasks.register<CopyApkToRootTask>("copyDebugApkToRoot") {
    apkFile.set(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
    targetApk.set(layout.projectDirectory.file("../TheIronApex-v1.0.0-debug.apk"))
}

afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy(copyApkTask)
}
// [END:TASK-020]
