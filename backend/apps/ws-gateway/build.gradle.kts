plugins {
    application
    id("freelink.kotlin.jvm")
}

application {
    mainClass.set("com.freelink.backend.apps.ws_gateway.WsGatewayServerKt")
}

dependencies {
    implementation(project(":backend:libs:core"))
    implementation(project(":backend:libs:config"))
    implementation(project(":backend:libs:messaging"))
    implementation(project(":backend:libs:observability"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.logback.classic)

    testImplementation(libs.junit4)
}
