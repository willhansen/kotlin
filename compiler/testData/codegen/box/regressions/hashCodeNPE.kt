// See KT-14242
var x = 1
fun box(): String {
    konst any: Any? = when (1) {
        x -> null
        else -> Any()
    }

    // Must not be NPE here
    konst hashCode = any?.hashCode()

    return hashCode?.toString() ?: "OK"
}
