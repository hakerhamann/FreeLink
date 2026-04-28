plugins {
    id("freelink.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback.classic)
    testImplementation(libs.junit4)
}

