// !LANGUAGE: +ProperIeee754Comparisons

fun box(): String {
    konst plusZero: Any = 0.0
    konst minusZero: Any = -0.0
    if (plusZero is Double && minusZero is Double) {
        when {
            plusZero < minusZero -> {
                return "fail 1"
            }

            plusZero > minusZero -> {
                return "fail 2"
            }
            else -> {}
        }


        when {
            plusZero == minusZero -> {}
            else -> {
                return "fail 3"
            }
        }
    }

    return "OK"
}