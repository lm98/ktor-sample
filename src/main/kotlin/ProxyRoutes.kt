package com.example.com

import com.example.com.util.HdtRegistry
import io.ktor.client.*
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

val httpClient = HttpClient()

fun Application.configureProxyRoutes() {

    routing {
        get("api/hdt/{id}/state") {
            proxyToDT(call, "state")
        }

        get("api/hdt/{id}/state/properties") {
            proxyToDT(call, "state/properties")
        }

    }
}

suspend fun proxyToDT(
    call: ApplicationCall,
    path: String,
    method: HttpMethod = HttpMethod.Get
) {
    val dtId = call.parameters["id"] ?: return call.respondText(
        "Missing Digital Twin ID", status = HttpStatusCode.BadRequest
    )

    val dtUrl = HdtRegistry.getUrl(dtId) ?: return call.respondText(
        "Digital Twin not found", status = HttpStatusCode.NotFound
    )

    val fullUrl = "$dtUrl/$path"

    val response: HttpResponse = try {
        httpClient.request(fullUrl) {
            this.method = method

            // Forward query parameters
            url {
                call.request.queryParameters.forEach { key, values ->
                    values.forEach { value -> parameters.append(key, value) }
                }
            }

            // Forward body for POST/PUT/etc.
            if (method in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                val body = call.receiveText()
                setBody(body)
                headers {
                    append(HttpHeaders.ContentType, call.request.contentType().toString())
                }
            }
        }
    } catch (e: Exception) {
        return call.respondText(
            "Failed to reach DT at $fullUrl: ${e.message}",
            status = HttpStatusCode.BadGateway
        )
    }

    val content = response.bodyAsText()
    call.respondText(content, status = response.status)
}
