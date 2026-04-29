import com.android.build.api.dsl.LibraryExtension

plugins {
}

extensions.configure<LibraryExtension> {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}
