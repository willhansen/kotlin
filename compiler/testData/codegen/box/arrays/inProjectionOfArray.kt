fun box(): String {
    konst x : Array<Array<*>> = arrayOf(arrayOf(1))
    konst y : Array<in Array<String>> = x

    if (y.size != 1) return "fail 1"

    y[0] = arrayOf("OK")

    return x[0][0] as String
}