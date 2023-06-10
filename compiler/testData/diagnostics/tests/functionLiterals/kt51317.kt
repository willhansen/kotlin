konst f: (String.() -> String)? = null

fun box(): String {
    konst g = when {
        f != null -> <!DEBUG_INFO_SMARTCAST!>f<!>
        else -> {
            { this + "K" }
        }
    }
    return g("O")
}