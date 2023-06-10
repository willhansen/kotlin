object OHolder {
    konst O = "O"
}

typealias OHolderAlias = OHolder

class KHolder {
    companion object {
        konst K = "K"
    }
}

typealias KHolderAlias = KHolder

fun box(): String = OHolderAlias.O + KHolderAlias.K
