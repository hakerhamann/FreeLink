plugins {
    id("freelink.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.argon2.jvm)
    testImplementation(libs.junit4)
}

