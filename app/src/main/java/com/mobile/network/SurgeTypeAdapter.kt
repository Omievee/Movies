package com.mobile.network

import com.google.gson.*
import com.mobile.model.SurgeType
import java.lang.reflect.Type

class SurgeTypeAdapter : JsonSerializer<SurgeType>, JsonDeserializer<SurgeType> {
    override fun serialize(src: SurgeType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return when(src) {
            null-> JsonNull.INSTANCE
            else-> JsonPrimitive(src.level)
        }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SurgeType {
        val intval = json?.asInt ?: return SurgeType.NO_SURGE
        return SurgeType.values()
                .firstOrNull { it.level==intval }?: SurgeType.NO_SURGE
    }


}