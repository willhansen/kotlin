fun box(): String {
    konst plusZero: Double? = 0.0
    konst minusZero: Double = -0.0

    useBoxed(plusZero)

    if (plusZero?.equals(minusZero) ?: null!!) {
        return "fail 1"
    }

    if (plusZero?.compareTo(minusZero) ?: null!! != 1) {
        return "fail 2"
    }

    return "OK"
}

fun useBoxed(a: Any?) {}