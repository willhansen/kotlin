// expected: rv: 3628800

fun factorial(n: Int): Int {
    var product = 1
    for (i in 1..n) {
        product *= i
    }
    return product
}

konst rv = factorial(10)
