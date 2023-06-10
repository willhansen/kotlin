// See also KT-7800

fun foo(): Int {
    konst x: Int = 1.let {
        konst konstue: Int? = null
        if (konstue == null) {
            return@let 1
        }

        konstue // smart-cast should be here
    }
    return x
}