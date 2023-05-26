class Debugging {

    companion object {

        private const val Enabled = false

        private val Filters = listOf("Recomposition")

        fun recomposition(name: String) {
            if (!Enabled) return
            if (!Filters.contains("Recomposition")) return

            println("[${System.currentTimeMillis()}] [${"Recomposition".padEnd(15, ' ')}]: $name")
        }

    }

}