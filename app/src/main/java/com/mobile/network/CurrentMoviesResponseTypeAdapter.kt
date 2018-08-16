package com.mobile.network

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.mobile.model.CapType
import com.mobile.model.Movie
import com.mobile.responses.CurrentMoviesResponse
import java.lang.reflect.Type

abstract class CurrentMoviesResponseTypeAdapter() : JsonDeserializer<CurrentMoviesResponse>, JsonSerializer<CurrentMoviesResponse> {

    abstract fun gson(): Gson

    override fun serialize(src: CurrentMoviesResponse?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        src ?: return JsonNull.INSTANCE
        val json = JsonObject()
        json.add("featured", JsonArray().apply {
            src.featured.forEach {
                add(gson().toJsonTree(it))
            }
        })
        src.categorizedMovies.forEach {
            json.add(it.first, JsonArray().apply {
                it.second.forEach {
                    add(gson().toJsonTree(it))
                }
            })
        }
        return json
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): CurrentMoviesResponse {
        val data = json.asJsonObject
        val featured = data.entrySet().firstOrNull { it.key.equals("featured", true) }?.value
        val others = data.entrySet().filterNot { it.key.equals("featured", ignoreCase = true) }
                .map {
                    val movieList: List<Movie> = gson().fromJson(it.value, object : TypeToken<List<Movie>>() {}.type)
                    Pair(it.key, movieList)
                }
        return CurrentMoviesResponse(featured = when (featured) {
            null -> emptyList()
            else -> gson().fromJson(featured, object : TypeToken<List<Movie>>() {}.type)
        },
                categorizedMovies = others)
    }

}
