data class Test(konst z1: Double, konst z2: Double?)

fun box(): String {
    konst x = Test(Double.NaN, Double.NaN)
    konst y = Test(Double.NaN, Double.NaN)

    return if (x == y) "OK" else "fail"
}