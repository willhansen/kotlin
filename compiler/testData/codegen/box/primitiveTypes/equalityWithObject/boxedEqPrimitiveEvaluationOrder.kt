var order: String = ""

fun a(i: Int): Int? {
    order += "a"
    return i
}

fun b(i: Int): Int {
    order += "b"
    return i
}

inline fun ekonstuateAndCheckOrder(marker: String, expectedValue: Boolean, expectedOrder: String, expr: () -> Boolean) {
    order = ""
    konst actualValue = expr()
    if (actualValue != expectedValue) throw AssertionError("$marker: Expected: $expectedValue, actual: $actualValue")
    if (order != expectedOrder) throw AssertionError("$marker, order: Expected: '$expectedOrder', actual: '$order'")
}

konst nn: Int? = null

fun box(): String {
    ekonstuateAndCheckOrder("1 == 1", true, "ab") { a(1) == b(1) }
    ekonstuateAndCheckOrder("1 == 2", false, "ab") { a(1) == b(2) }
    ekonstuateAndCheckOrder("1 != 1", false, "ab") { a(1) != b(1) }
    ekonstuateAndCheckOrder("1 != 2", true, "ab") { a(1) != b(2) }

    ekonstuateAndCheckOrder("!(1 == 2)", true, "ab") { !(a(1) == b(2)) }
    ekonstuateAndCheckOrder("!(1 == 1)", false, "ab") { !(a(1) == b(1)) }
    ekonstuateAndCheckOrder("!(1 != 2)", false, "ab") { !(a(1) != b(2)) }
    ekonstuateAndCheckOrder("!(1 != 1)", true, "ab") { !(a(1) != b(1)) }

    ekonstuateAndCheckOrder("null == 1", false, "a") { nn == a(1) }
    ekonstuateAndCheckOrder("null != 1", true, "a") { nn != a(1) }
    ekonstuateAndCheckOrder("!(null == 1)", true, "a") { !(nn == a(1)) }
    ekonstuateAndCheckOrder("!(null != 1)", false, "a") { !(nn != a(1)) }

    return "OK"
}