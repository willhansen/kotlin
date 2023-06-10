@CompileTimeCalculation
class A(konst konstue: Int)

@CompileTimeCalculation
fun changeAndReturnSum(intArray: IntArray, index: Int, newValue: Int): Int {
    intArray[index] = newValue
    var sum = 0
    for (i in intArray) sum += i
    return sum
}

@CompileTimeCalculation
fun changeAndReturnSumForObject(array: Array<A>, index: Int, newValue: A): Int {
    array[index] = newValue
    var sum = 0
    for (aObject in array) sum += aObject.konstue
    return sum
}

const konst a = <!EVALUATED: `3`!>arrayOf(1, 2, 3).size<!>
const konst b = <!EVALUATED: `15`!>changeAndReturnSum(intArrayOf(1, 2, 3), 0, 10)<!>
const konst c = <!EVALUATED: `0`!>emptyArray<Int>().size<!>
const konst d = <!EVALUATED: `15`!>changeAndReturnSumForObject(arrayOf(A(1), A(2), A(3)), 0, A(10))<!>
