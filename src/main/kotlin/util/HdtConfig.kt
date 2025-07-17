package com.example.com.util

import io.github.lm98.whdt.core.hdt.model.property.Property
import kotlinx.serialization.Serializable

@Serializable
data class HdtConfig(
    val id: String,
    val properties: List<Property>
)