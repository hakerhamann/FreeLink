rootProject.name = "FreeLink"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include(
    ":android:app",
    ":android:core:common",
    ":android:core:model",
    ":android:core:designsystem",
    ":android:core:ui",
    ":android:core:navigation",
    ":android:core:network",
    ":android:core:database",
    ":android:core:datastore",
    ":android:core:encryption",
    ":android:core:media",
    ":android:core:updates",
    ":android:core:notifications",
    ":android:core:testing",
    ":android:feature:auth",
    ":android:feature:chatlist",
    ":android:feature:people",
    ":android:feature:spaces",
    ":android:feature:chat",
    ":android:feature:group",
    ":android:feature:profile",
    ":android:feature:calls",
    ":android:feature:settings",
    ":android:feature:media_gallery",
    ":android:feature:devices",
    ":android:feature:archive",
    ":android:benchmark",
    ":android:baselineprofile",
    ":backend:apps:api",
    ":backend:apps:ws-gateway",
    ":backend:apps:worker",
    ":backend:libs:core",
    ":backend:libs:config",
    ":backend:libs:db",
    ":backend:libs:auth",
    ":backend:libs:crypto",
    ":backend:libs:messaging",
    ":backend:libs:groups",
    ":backend:libs:media",
    ":backend:libs:calls",
    ":backend:libs:updates",
    ":backend:libs:privacy",
    ":backend:libs:notifications",
    ":backend:libs:observability"
)
