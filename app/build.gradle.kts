plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.agmstech.princessgame"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.agmstech.princessgame"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    // A shared, non-secret debug key (password "android"), so the ready-made APK
    // in apk/ and anything built from Android Studio can update one another
    // without uninstalling, which would erase the child's saved progress.
    // Use your own upload key for Google Play.
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

// `./gradlew testDebugUnitTest -Dpreview.dir=/some/dir` writes SVG previews of all the art.
tasks.withType<Test>().configureEach {
    System.getProperty("preview.dir")?.let { systemProperty("preview.dir", it) }
}
