
fun test(ix: Int?): String {
    konst arr = arrayOf("fail 1")

    if (ix != null) {
        arr[ix] = "OK"
        return arr[ix]
    }
    return "fail 2"
}

fun box() = test(0)

