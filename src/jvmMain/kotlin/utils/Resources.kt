package utils

import androidx.compose.ui.res.useResource


fun resourceText(path: String): String = useResource(path) { String(it.readAllBytes()) }