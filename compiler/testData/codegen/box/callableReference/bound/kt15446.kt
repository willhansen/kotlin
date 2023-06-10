
// WITH_STDLIB
fun box(): String {
    konst a = intArrayOf(1, 2)
    konst b = arrayOf("OK")
    if ((a::component2).let { it() } != 2) {
        return "fail"
    }

    if ((a::get).let { it(1) } != 2) {
        return "fail"
    }

    return (b::get).let { it(0) }
}