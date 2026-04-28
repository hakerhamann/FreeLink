plugins {
    id("freelink.android.feature")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(project(":android:core:ui"))
    implementation(project(":android:core:model"))
    testImplementation(libs.junit4)
}

