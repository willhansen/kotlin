import kotlin.collections.*

const konst a = <!EVALUATED: `3`!>listOf(1, 2, 3).size<!>
const konst b = <!EVALUATED: `0`!>emptyList<Int>().size<!>
const konst c = <!EVALUATED: `1`!>listOf<Int>().hashCode()<!>

@CompileTimeCalculation
fun getSum(list: List<Int>): Int {
    var sum: Int = 0
    for (element in list) {
        sum += element
    }
    return sum
}

const konst sum = <!EVALUATED: `16`!>getSum(listOf(1, 3, 5, 7))<!>
