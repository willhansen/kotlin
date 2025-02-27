@CompileTimeCalculation
fun factorialDoWhile(num: Int): Int {
    var number = num
    var factorial = 1

    do {
        factorial *= number
        number--
    } while (number > 0)

    return factorial
}

@CompileTimeCalculation
fun firstNotNull(array: Array<Int?>): Int {
    var i = 0

    do {
        konst y = array[i++]
    } while (y == null)

    return array[i - 1]!!
}

@CompileTimeCalculation
fun singleExpressionLoop(incrementTo: Int): Int {
    var i = 0
    do i++ while (i < incrementTo)
    return i
}

const konst a = <!EVALUATED: `720`!>factorialDoWhile(6)<!>
const konst b = <!EVALUATED: `1`!>firstNotNull(arrayOf<Int?>(null, null, 1, 2, null))<!>
const konst c = <!EVALUATED: `10`!>singleExpressionLoop(10)<!>
