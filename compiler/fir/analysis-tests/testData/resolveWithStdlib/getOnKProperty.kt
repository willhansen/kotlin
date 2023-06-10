konst Any?.meaning: Int
    get() = 42

fun test() {
    konst f = Any?::meaning
    f.get(null)
    f.get("")
}