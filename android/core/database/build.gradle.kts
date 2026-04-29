plugins {
    id("freelink.android.library")
    alias(libs.plugins.ksp)
}

dependencies {
    api(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(project(":android:core:model"))
    ksp(libs.room.compiler)
    testImplementation(libs.junit4)
}

