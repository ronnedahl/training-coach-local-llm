plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// sherpa-onnx (Piper TTS) ships as a prebuilt Android AAR on GitHub releases.
// It is not on Maven Central and JitPack fails to build it, so we fetch the AAR
// into app/libs on demand instead of committing a 54 MB binary (app/libs is
// gitignored). The download runs once, before the build, then stays cached.
val sherpaOnnxVersion = libs.versions.sherpaOnnx.get()
val sherpaOnnxAar = layout.projectDirectory.file("libs/sherpa-onnx-$sherpaOnnxVersion.aar")

val downloadSherpaOnnx by tasks.registering {
    description = "Downloads the sherpa-onnx Android AAR into app/libs if missing."
    val target = sherpaOnnxAar.asFile
    outputs.file(target)
    doLast {
        if (target.exists() && target.length() > 0L) return@doLast
        target.parentFile.mkdirs()
        val url =
            "https://github.com/k2-fsa/sherpa-onnx/releases/download/" +
                "v$sherpaOnnxVersion/sherpa-onnx-$sherpaOnnxVersion.aar"
        logger.lifecycle("Downloading sherpa-onnx $sherpaOnnxVersion AAR from $url")
        val connection = (uri(url).toURL().openConnection() as java.net.HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 30_000
            readTimeout = 60_000
        }
        connection.inputStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        logger.lifecycle("Downloaded sherpa-onnx AAR (${target.length()} bytes)")
    }
}

tasks.named("preBuild") { dependsOn(downloadSherpaOnnx) }

android {
    namespace = "dev.peterbot.traingcoach"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "dev.peterbot.traingcoach"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Target device is the Galaxy A33 (arm64-v8a). Shipping only this ABI
        // keeps the sherpa-onnx native libs from bloating the APK. Add other
        // ABIs here if you need an emulator.
        ndk {
            abiFilters += "arm64-v8a"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(files(sherpaOnnxAar))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}