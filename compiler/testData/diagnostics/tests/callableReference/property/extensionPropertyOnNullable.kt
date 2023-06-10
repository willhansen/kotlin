// FIR_IDENTICAL
// !CHECK_TYPE

konst Any?.meaning: Int
    get() = 42

fun test() {
    konst f = Any?::meaning
    checkSubtype<Int>(f.get(null))
    checkSubtype<Int>(f.get(""))
}
