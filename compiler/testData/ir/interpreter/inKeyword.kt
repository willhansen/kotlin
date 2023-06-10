import kotlin.collections.*

@CompileTimeCalculation
fun numberIsInArray(array: IntArray, number: Int): Boolean {
    return number in array
}

@CompileTimeCalculation
fun konstueIsInArray(array: Array<Any>, konstue: Any?): Boolean {
    return konstue in array
}

const konst a1 = <!EVALUATED: `true`!>numberIsInArray(intArrayOf(1, 2, 3), 1)<!>
const konst a2 = <!EVALUATED: `false`!>numberIsInArray(intArrayOf(1, 2, 3), -1)<!>

const konst b1 = <!EVALUATED: `true`!>konstueIsInArray(arrayOf(1, 2, 3), 1)<!>
const konst b2 = <!EVALUATED: `false`!>konstueIsInArray(arrayOf(1, 2, 3), -1)<!>
const konst b3 = <!EVALUATED: `true`!>konstueIsInArray(arrayOf(1, 2.0f, "3"), "3")<!>
const konst b4 = <!EVALUATED: `false`!>konstueIsInArray(arrayOf(1, 2.0f, "3"), null)<!>
const konst b5 = <!EVALUATED: `false`!>konstueIsInArray(arrayOf(1, 2.0f, "3"), 1.0f)<!>
