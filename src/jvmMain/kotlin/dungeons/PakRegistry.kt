package dungeons

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import pak.PakIndex
import pak.PathPakFilter
import parsers.objects.fields.Guid
import java.io.File



class PakRegistry {

    companion object {

        private var _index: PakIndex? by mutableStateOf(null)
        val index by derivedStateOf { _index!! }
        val initialized by derivedStateOf { _index != null }

        fun initialize() {
            if (_index != null) return

            val pakPath = findPakPath()

            val newIndex = PakIndex(
                File(pakPath).listFiles()!!.sortedBy { it.name },
                useCache = true,
                caseSensitive = true,
                filter = PathPakFilter(listOf("/Dungeons/Content"), false)
            )
            newIndex.useKey(Guid.Zero, Keyset.PakKey)

            _index = newIndex

            Database.register(DatabaseGenerator.parsePak(newIndex))
        }

        fun findPakPath() =
            "/home/hoonkun/.local/share/Steam/steamapps/compatdata/2764086203/pfx/drive_c/users/steamuser/AppData/Local/Mojang/products/dungeons/dungeons/Dungeons/Content/Paks"

    }

}