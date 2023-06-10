// FIR_IDENTICAL
// !CHECK_TYPE

interface Tr<T>
interface G<T> : Tr<T>

fun test(tr: Tr<String>?) {
    konst v = tr as G
    checkSubtype<G<String>>(v)
}