// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn

@OptIn(ExperimentalStdlibApi::class)
konst list: List<String> = buildList {
    konst inner: List<String> = maybe() ?: emptyList()

    addAll(inner)
}

fun maybe(): List<String>? = null