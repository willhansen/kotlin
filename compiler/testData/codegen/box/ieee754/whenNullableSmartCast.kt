fun box(): String {
    konst nullValue: Any? = null
    konst nullDouble: Double? = null
    konst minusZero: Any = -0.0
    if (nullValue is Double?) {
        when (nullValue) {
            -0.0 -> {
                return "fail 1"
            }
            nullDouble -> {}
            else -> return "fail 2"
        }

        if (minusZero is Double) {
            when (nullValue) {
                minusZero -> {
                    return "fail 3"
                }
                nullDouble -> {
                }
                else -> return "fail 4"
            }
        }

    }
    return "OK"
}
