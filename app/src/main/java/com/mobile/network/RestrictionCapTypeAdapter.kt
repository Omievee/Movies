package com.mobile.network

import com.google.gson.*
import com.mobile.model.CapType
import java.lang.reflect.Type

class RestrictionCapTypeAdapter : JsonSerializer<RestrictionsCheckType>, JsonDeserializer<RestrictionsCheckType> {
    override fun serialize(src: RestrictionsCheckType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return when (src) {
            null -> JsonNull.INSTANCE
            else -> JsonPrimitive(src.name.toLowerCase())
        }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RestrictionsCheckType {
        val intval = json?.asString ?: return RestrictionsCheckType.NO_RESTRICTIONS
        return RestrictionsCheckType.values()
                .firstOrNull { it.name.toLowerCase() == intval } ?: RestrictionsCheckType.NO_RESTRICTIONS
    }


}