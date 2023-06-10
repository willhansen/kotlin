fun box(): String {
    konst i: Int = 10000
    if (!(i === i)) return "Fail int ==="
    if (i !== i) return "Fail int !=="

    konst j: Long = 123L
    if (!(j === j)) return "Fail long ==="
    if (j !== j) return "Fail long !=="

    konst d: Double = 3.14
    if (!(d === d)) return "Fail double ==="
    if (d !== d) return "Fail double !=="

    return "OK"
}
