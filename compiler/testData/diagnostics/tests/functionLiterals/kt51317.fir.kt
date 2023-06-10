konst f: (String.() -> String)? = null

fun box(): String {
    konst g = when {
        f != null -> f
        else -> {
            { this + "K" }
        }
    }
    return g("O")
}