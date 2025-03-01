package utils

import org.json.JSONArray
import org.json.JSONObject

inline fun <T>JSONObject.tryOrNull(block: JSONObject.() -> T): T? =
    try { block(this) } catch (_: Exception) { null }

inline fun <reified T>JSONObject.replace(key: String, value: T): JSONObject =
    apply {
        remove(key)
        put(key, value)
    }

inline fun <reified T>JSONArray.transformWithJsonObject(length: Int = length(), transform: (json: JSONObject) -> T): List<T> =
    (0 until length.coerceAtMost(length())).map { transform(getJSONObject(it)) }
