fun box(): String {
    konst plusZero: Any = 0.0
    konst minusZero: Any = -0.0
    if ((minusZero as Double) < (plusZero as Double)) return "fail 0"

    konst plusZeroF: Any = 0.0F
    konst minusZeroF: Any = -0.0F
    if ((minusZeroF as Float) < (plusZeroF as Float)) return "fail 1"

    if ((minusZero as Double) != (plusZero as Double)) return "fail 3"

    if ((minusZeroF as Float) != (plusZeroF as Float)) return "fail 4"

    return "OK"
}