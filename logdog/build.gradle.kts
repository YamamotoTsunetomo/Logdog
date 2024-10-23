import java.io.FileInputStream
import java.util.Properties

plugins {
    id("maven-publish")
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.tsunetomo.logdog"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

fun getVersionName(): String {
    return "1.0.6"
}

fun getArtifactId(): String {
    return "logdog"
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            run {
                groupId = "com.github.yamamototsunetomo"
                artifactId = getArtifactId()
                version = getVersionName()
                artifact("$buildDir/outputs/aar/${getArtifactId()}-release.aar")
            }
        }
    }

    tasks.named<PublishToMavenLocal>("publishGprPublicationToMavenLocal") {
        dependsOn(tasks.named("bundleReleaseAar"))
    }

}

dependencies {
    implementation(libs.okhttp)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}