package com.mobile.network

import com.google.gson.*
import com.mobile.model.ParcelableDate
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter : JsonSerializer<ParcelableDate>, JsonDeserializer<ParcelableDate> {

    companion object {
        private val PERIOD = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        private val COLON = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US)
        private val T = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS", Locale.US)
        private val YMD = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val HM = SimpleDateFormat("HH:mm:ss", Locale.US)
        private val HMA = SimpleDateFormat("hh:mm a", Locale.US)
        private val NUMBER_REGEX = "-?\\d+(\\.\\d+)?".toRegex()

        fun deseralize(dateString: String? = null): ParcelableDate? {
            val str = dateString ?: return null
            val sdf: SimpleDateFormat = when {
                str.length > 8 -> {
                    val ch = str.get(str.length - 4)
                    val containsT = str.indexOf('T')
                    when {
                        containsT > -1 -> {
                            var last = str.lastIndexOf('.')
                            if (last == -1) {
                                last = str.lastIndexOf(':')
                            }
                            return ParcelableDate(str, HM.parse(str.substring(containsT+1, last)).time)
                        }
                        str.length==10 && str.get(4)=='-' -> YMD
                        ch == ':' -> COLON
                        NUMBER_REGEX.matches(str) -> return ParcelableDate(str, str.toLong())
                        else -> PERIOD
                    }
                }
                else -> HMA
            }
            return ParcelableDate(str, sdf.parse(str).time)
        }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ParcelableDate? {
        val str = json?.asString ?: return null
        return deseralize(str)
    }

    override fun serialize(src: ParcelableDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return when (src?.timeAsString) {
            null -> JsonNull.INSTANCE
            else -> JsonPrimitive(src.timeAsString)
        }
    }

}