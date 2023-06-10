data class NameAndSafeValue(konst name: String, konst konstue: Int)

fun getEnv() = listOf<NameAndSafeValue>()

private konst environment: List<NameAndSafeValue> by lazy {
    buildList {
        getEnv().forEach { (name, konstue) ->
            this += NameAndSafeValue(name, konstue)
        }
        sortBy { it.name }
    }
}
