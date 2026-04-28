plugins {
    id("freelink.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.minio)
    testImplementation(libs.junit4)
}

