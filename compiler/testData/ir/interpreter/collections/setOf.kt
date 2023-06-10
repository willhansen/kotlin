import kotlin.collections.*

const konst a1 = <!EVALUATED: `3`!>setOf(1, 2, 3).size<!>
const konst a2 = <!EVALUATED: `3`!>setOf(1, 2, 3, 3, 2, 1).size<!>
const konst b = <!EVALUATED: `0`!>emptySet<Int>().size<!>
const konst c = <!EVALUATED: `0`!>setOf<Int>().hashCode()<!>

@CompileTimeCalculation
fun getSum(set: Set<Int>): Int {
    var sum: Int = 0
    for (element in set) {
        sum += element
    }
    return sum
}

const konst sum = <!EVALUATED: `16`!>getSum(setOf(1, 3, 5, 7, 7, 5))<!>
