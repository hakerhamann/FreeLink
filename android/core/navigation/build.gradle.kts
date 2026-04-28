plugins {
    id("freelink.android.library")
    id("freelink.android.compose")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(project(":android:core:ui"))
    implementation(project(":android:feature:auth"))
    implementation(project(":android:feature:chatlist"))
    implementation(project(":android:feature:devices"))
    implementation(project(":android:feature:profile"))
    implementation(project(":android:feature:settings"))
    testImplementation(libs.junit4)
}

