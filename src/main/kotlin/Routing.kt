package com.example.com

import com.example.com.util.HdtConfig
import io.github.lm98.whdt.core.hdt.HumanDigitalTwin
import io.github.lm98.whdt.core.hdt.interfaces.digital.MqttDigitalInterface
import io.github.lm98.whdt.core.hdt.interfaces.physical.MqttPhysicalInterface
import io.github.lm98.whdt.core.hdt.model.Model
import io.github.lm98.whdt.wldt.plugin.execution.WldtApp
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val app = WldtApp()

    routing {
        post("new-dt") {
            try {
                val hdtConfig = call.receive<HdtConfig>()
                val pI = MqttPhysicalInterface(
                    clientId = hdtConfig.id,
                    properties = hdtConfig.properties
                )
                val dI = MqttDigitalInterface(
                    clientId = hdtConfig.id,
                    properties = hdtConfig.properties
                )
                val hdt = HumanDigitalTwin(
                    id = hdtConfig.id,
                    models = listOf(Model(hdtConfig.properties)),
                    physicalInterfaces = listOf(pI),
                    digitalInterfaces = listOf(dI),
                )
                app.addDt(hdt).startDt(hdt.id)
                call.respond(HttpStatusCode.Created, hdt)
            } catch (e: Exception) {
                println("Deserialization failed: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, "Invalid HumanDigitalTwin JSON: ${e.message}")
            }
        }

        post("receive-csv") {
            val text = call.receiveText()
            call.respondText(text)
        }
    }
}
