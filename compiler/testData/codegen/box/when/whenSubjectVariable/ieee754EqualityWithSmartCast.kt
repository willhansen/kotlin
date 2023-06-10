// !LANGUAGE: +VariableDeclarationInWhenSubject +ProperIeee754Comparisons

konst az: Any = -0.0
konst afz: Any = -0.0f

fun box(): String {
    konst y = az
    when (y) {
        !is Double -> throw AssertionError()
        0.0 -> {}
        else -> throw AssertionError()
    }
    konst yy = afz
    when (yy) {
        !is Float -> throw AssertionError()
        0.0 -> {}
        else -> throw AssertionError()
    }

    testDoubleAsUpperBound(-0.0)

    return "OK"
}

fun <T: Double> testDoubleAsUpperBound(v: T): Boolean {
    return when (konst a = v*v) {
        0.0 -> true
        else -> throw AssertionError()
    }
}
