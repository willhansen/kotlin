// FIR_IDENTICAL
data class My(konst x: Unit)

fun foo(my: My?): Int? {
    konst x = my?.x
    // ?. is required here
    return x?.hashCode()
}