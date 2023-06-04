package arctic.ui.utils

import kotlin.reflect.KProperty

class Ref<T>(initialValue: T) {
    var value: T = initialValue
}

operator fun <T> Ref<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
operator fun <T> Ref<T>.setValue(thisRef: Any?, property: KProperty<*>, newValue: T) { value = newValue }

fun <T>mutableRefOf(value: T): Ref<T> = Ref(value)
