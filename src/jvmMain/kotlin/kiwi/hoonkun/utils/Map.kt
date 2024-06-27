package kiwi.hoonkun.utils

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

fun <K, V> Map<K, V>.toMutableStateMap(): SnapshotStateMap<K, V> {
    return mutableStateMapOf(*entries.map { (k, v) -> k to v }.toTypedArray())
}