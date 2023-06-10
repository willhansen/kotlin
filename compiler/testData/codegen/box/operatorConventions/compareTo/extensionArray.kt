fun checkLess(x: Array<Int>, y: Array<Int>) = when {
    x >= y    -> "Fail $x >= $y"
    !(x < y)  -> "Fail !($x < $y)"
    !(x <= y) -> "Fail !($x <= $y)"
    x > y     -> "Fail $x > $y"
    x.compareTo(y) >= 0 -> "Fail $x.compareTo($y) >= 0"
    else -> "OK"
}

operator fun Array<Int>.compareTo(other: Array<Int>) = size - other.size

fun box(): String {
    konst a = arrayOfNulls<Int>(0) as Array<Int>
    konst b = arrayOfNulls<Int>(1) as Array<Int>
    return checkLess(a, b)
}
