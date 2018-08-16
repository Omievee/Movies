package com.mobile.network

import com.google.gson.*
import com.mobile.model.CapType
import java.lang.reflect.Type

class CapTypeAdapter : JsonSerializer<CapType>, JsonDeserializer<CapType> {
    override fun serialize(src: CapType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return when (src) {
            null -> JsonNull.INSTANCE
            else -> JsonPrimitive(src.name.toLowerCase())
        }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): CapType {
        val intval = json?.asString ?: return CapType.SOFT
        return CapType.values()
                .firstOrNull { it.name.toLowerCase() == intval } ?: CapType.SOFT
    }


}