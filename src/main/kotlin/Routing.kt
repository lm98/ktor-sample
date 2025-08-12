package com.example.com

import com.example.com.util.HdtConfig
import com.example.com.util.HdtConfig.Companion.toHumanDigitalTwin
import com.example.com.util.HdtRegistry
import io.github.lm98.whdt.core.hdt.interfaces.digital.HttpDigitalInterface
import io.github.lm98.whdt.csv.parser.ParserCSV
import io.github.lm98.whdt.wldt.plugin.execution.WldtApp
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val app = WldtApp()

    routing {
        post("api/hdt/new") {
            try {
                val hdtConfig = call.receive<HdtConfig>()
                val hdt = toHumanDigitalTwin(hdtConfig)
                HdtRegistry.register(hdt.id)
                val httpDI = HttpDigitalInterface(
                    host = HdtRegistry.HDT_HTTP_HOST,
                    port = HdtRegistry.getPort(hdt.id)!!,
                    properties = hdtConfig.properties,
                    id = hdtConfig.id,
                )
                val newDt = hdt.copy(digitalInterfaces = hdt.digitalInterfaces + listOf(httpDI))
                app.addStart(newDt)
                call.respond(HttpStatusCode.Created, newDt)
            } catch (e: Exception) {
                println("Deserialization failed: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, "Invalid HumanDigitalTwin JSON: ${e.message}")
            }
        }

        post("api/hdt/new/csv") {
            try {
                val text = call.receiveText()
                val parser = ParserCSV.createParserCSV()
                val map = parser.parsing(text)
                //println(map.toString())
                //csv contains info about several dts
                val hdts = map.map {
                    val id = it.key
                    val properties = it.value
                    toHumanDigitalTwin(HdtConfig("hdt-$id", properties))
                }
                app.addStartAll(hdts)
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid HumanDigitalTwin CSV: ${e.message}")
            }
        }

        get("api/hdt") {
            val dts = HdtRegistry.getRegisteredIds()
            println("Requested dts: $dts")
            call.respond(dts)
        }
    }
}
