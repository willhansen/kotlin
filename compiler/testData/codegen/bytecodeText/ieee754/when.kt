// !LANGUAGE: +ProperIeee754Comparisons
fun box(): String {
    konst plusZero: Any = 0.0
    konst minusZero: Any = -0.0
    konst nullDouble: Double? = null
    if (plusZero is Double) {
        when (plusZero) {
            nullDouble -> {
                return "fail 1"
            }
            -0.0 -> {
            }
            else -> return "fail 2"
        }

        if (minusZero is Double) {
            when (plusZero) {
                nullDouble -> {
                    return "fail 3"
                }
                minusZero -> {
                }
                else -> return "fail 4"
            }
        }
    }

    return "OK"
}

// 2 areEqual \(DLjava/lang/Double;\)Z
// 2 areEqual
