package extensions

import org.json.JSONArray
import org.json.JSONObject

inline fun <T>JSONObject.safe(block: JSONObject.() -> T): T? {
    return try { block(this) } catch (_: Exception) { null }
}

fun <T> JSONArray.toJsonObjectArray(transform: (json: JSONObject) -> T): List<T> {
    return (0 until length()).map { transform(getJSONObject(it)) }
}