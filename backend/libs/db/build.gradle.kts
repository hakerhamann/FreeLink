plugins {
    id("freelink.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.postgresql)
    implementation(libs.lettuce.core)
    testImplementation(libs.junit4)
}

