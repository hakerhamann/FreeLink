plugins {
    id("freelink.android.application")
}

dependencies {
    implementation(project(":android:core:designsystem"))
    implementation(project(":android:core:navigation"))
    implementation(project(":android:core:datastore"))
    implementation(project(":android:core:model"))

    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    testImplementation(libs.junit4)
    debugImplementation(libs.compose.ui.tooling)
}
