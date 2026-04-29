plugins {
    id("freelink.android.library")
}

dependencies {
    implementation(project(":android:core:model"))
    testImplementation(libs.junit4)
}

