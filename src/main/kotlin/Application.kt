package com.example.com

import io.github.lm98.whdt.core.serde.modules.hdtModule
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            serializersModule = hdtModule
            classDiscriminator = "type" // Needed for sealed polymorphic serialization
            encodeDefaults = true
        })
    }

    install(CORS) {
        allowHost("localhost:3000")

        allowMethod(HttpMethod.Options) // 👈 Required for preflight
        allowMethod(HttpMethod.Post)    // 👈 Allow POST
        allowHeader(HttpHeaders.ContentType) // 👈 Allow content-type header
        allowHeader(HttpHeaders.Authorization) // (Optional)

    }

    configureRouting()
    configureProxyRoutes()
}
