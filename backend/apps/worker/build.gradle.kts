plugins {
    application
    id("freelink.kotlin.jvm")
}

application {
    mainClass.set("com.freelink.backend.apps.worker.WorkerMainKt")
}

dependencies {
    implementation(project(":backend:libs:core"))
    implementation(project(":backend:libs:config"))
    implementation(project(":backend:libs:db"))
    implementation(project(":backend:libs:media"))
    implementation(project(":backend:libs:updates"))
    implementation(project(":backend:libs:notifications"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback.classic)

    testImplementation(libs.junit4)
}
