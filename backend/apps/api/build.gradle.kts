plugins {
    application
    id("freelink.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

application {
    mainClass.set("com.freelink.backend.apps.api.ApiServerKt")
}

dependencies {
    implementation(project(":backend:libs:core"))
    implementation(project(":backend:libs:config"))
    implementation(project(":backend:libs:db"))
    implementation(project(":backend:libs:auth"))
    implementation(project(":backend:libs:crypto"))
    implementation(project(":backend:libs:messaging"))
    implementation(project(":backend:libs:groups"))
    implementation(project(":backend:libs:media"))
    implementation(project(":backend:libs:privacy"))
    implementation(project(":backend:libs:notifications"))
    implementation(project(":backend:libs:observability"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback.classic)

    testImplementation(libs.junit4)
}
