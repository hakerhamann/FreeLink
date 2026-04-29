plugins {
    base
    alias(libs.plugins.kotlin.serialization) apply false
}

allprojects {
    group = "com.freelink"
    version = "0.1.0-SNAPSHOT"
}
