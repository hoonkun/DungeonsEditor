package extensions

import org.json.JSONArray
import org.json.JSONObject

inline fun <T>JSONObject.safe(block: JSONObject.() -> T): T? {
    return try { block(this) } catch (_: Exception) { null }
}

fun JSONArray.toJsonObjectArray(): List<JSONObject> {
    return (0 until length()).map { getJSONObject(it) }
}