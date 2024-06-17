package kiwi.hoonkun.utils

import androidx.compose.ui.res.useResource

private object Dummy

open class ResourceReadable {
    fun resourceText(path: String): String = useResource(path) { String(it.readAllBytes()) }
}