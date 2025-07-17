package com.example.com.util

import io.github.lm98.whdt.core.hdt.HumanDigitalTwin
import io.github.lm98.whdt.core.hdt.interfaces.digital.MqttDigitalInterface
import io.github.lm98.whdt.core.hdt.interfaces.physical.MqttPhysicalInterface
import io.github.lm98.whdt.core.hdt.model.Model
import io.github.lm98.whdt.core.hdt.model.property.Property
import kotlinx.serialization.Serializable

@Serializable
data class HdtConfig(
    val id: String,
    val properties: List<Property>
) {
    companion object {
        fun toHumanDigitalTwin(hdtConfig: HdtConfig): HumanDigitalTwin {
            val pI = MqttPhysicalInterface(
                clientId = hdtConfig.id,
                properties = hdtConfig.properties
            )
            val mqttDI = MqttDigitalInterface(
                clientId = hdtConfig.id,
                properties = hdtConfig.properties
            )
            return HumanDigitalTwin(
                id = hdtConfig.id,
                models = listOf(Model(hdtConfig.properties)),
                physicalInterfaces = listOf(pI),
                digitalInterfaces = listOf(mqttDI),
            )
        }
    }
}