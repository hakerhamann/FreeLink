plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.5.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.52")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.24-1.0.20")
}
