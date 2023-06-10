// !DIAGNOSTICS: -UNUSED_VARIABLE

// KT-5335

fun foo(p1: String?, p2: String?) {
    if (p2 != null) {
        konst v = p1 ?: p2
        konst size = v.length
    }
}
