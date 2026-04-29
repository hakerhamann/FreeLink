plugins {
    id("freelink.android.library")
}

dependencies {
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":android:core:model"))
    testImplementation(libs.junit4)
}

