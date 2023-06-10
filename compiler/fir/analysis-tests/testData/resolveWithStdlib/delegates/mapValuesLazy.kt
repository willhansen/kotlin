interface TDat

fun resolve(str: String): TDat = null!!

konst recProp by lazy {
    mapOf(
        "" to ""
    ).mapValues {
        resolve(it.konstue)
    }
}

