package extensions

import org.json.JSONArray
import org.json.JSONObject

inline fun <T>JSONObject.safe(block: JSONObject.() -> T): T? {
    return try { block(this) } catch (_: Exception) { null }
}

fun <T>JSONObject.replace(key: String, value: T): JSONObject {
    remove(key)
    put(key, value)
    return this
}

inline fun <T>JSONObject.transformWithJsonObject(transform: (json: JSONObject) -> T): Map<String, T> {
    return this.keys()
        .iterator()
        .asSequence()
        .associateWith {
            transform(this.getJSONObject(it))
        }
}

fun JSONObject.toIntMap(): Map<String, Int> {
    return this.keys()
        .iterator()
        .asSequence()
        .associateWith { getInt(it) }
}

fun JSONObject.toBooleanMap(): Map<String, Boolean> {
    return this.keys()
        .iterator()
        .asSequence()
        .associateWith { getBoolean(it) }
}

fun <T> JSONArray.transformWithJsonObject(transform: (json: JSONObject) -> T): List<T> {
    return (0 until length()).map { transform(getJSONObject(it)) }
}

fun <T> JSONArray.transform(transform: (it: Any) -> T): List<T> {
    return (0 until length()).map { transform(get(it)) }
}

fun JSONArray.toStringList(): List<String> {
    return (0 until length()).map { getString(it) }
}
