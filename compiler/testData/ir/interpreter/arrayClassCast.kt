@CompileTimeCalculation
fun <T> getArray(array: Array<T>) = array

const konst a1 = <!EVALUATED: `true`!>getArray(arrayOf(1, 2.0, "3")) as? Array<Int> == null<!>
const konst a2 = <!EVALUATED: `false`!>getArray(arrayOf(1, 2, 3)) as? Array<Int> == null<!>
const konst a3 = <!EVALUATED: `true`!>getArray(arrayOf(1, 2, 3)) as? Array<Double> == null<!>
const konst a4 = <!EVALUATED: `false`!>getArray(arrayOf(1, 2, 3)) as? Array<Number> == null<!>

const konst b1 = <!EVALUATED: `true`!>arrayOf(arrayOf(1, 2, 3)) as? Array<Array<String>> == null<!>
const konst b2 = <!EVALUATED: `false`!>arrayOf(arrayOf(1, 2, 3)) as? Array<Array<Int>> == null<!>
const konst b3 = <!EVALUATED: `false`!>arrayOf(arrayOf(1, 2, 3)) as? Array<Array<Number>> == null<!>

const konst c1 = <!EVALUATED: `false`!>arrayOf(arrayOf(1, 2, 3), arrayOf("1", "2", "3"))[0] as? Array<Int> == null<!>
const konst c2 = <!EVALUATED: `false`!>arrayOf(arrayOf(1, 2, 3), arrayOf("1", "2", "3"))[1] as? Array<String> == null<!>

@CompileTimeCalculation
fun <T, E> combineArrays(array1: Array<T>, array2: Array<E>) = arrayOf(array1, array2)

const konst d1 = <!EVALUATED: `true`!>combineArrays(arrayOf(1, 2, 3), arrayOf(1, 2, 3)) as? Array<Array<Int>> == null<!>
const konst d2 = <!EVALUATED: `true`!>combineArrays(arrayOf(1, 2, 3), arrayOf(1, 2, 3)) as? Array<Array<Number>> == null<!>
const konst d3 = <!EVALUATED: `false`!>combineArrays(arrayOf(1, 2, 3), arrayOf(1, 2, 3)) as? Array<Array<Any>> == null<!>
const konst d4 = <!EVALUATED: `false`!>combineArrays(arrayOf(1, 2, 3), arrayOf(1, 2, 3)) as? Array<Array<*>> == null<!>
const konst d5 = <!EVALUATED: `false`!>combineArrays(arrayOf(1, 2, 3), arrayOf("1", "2", "3"))[0] as? Array<Int> == null<!>
const konst d6 = <!EVALUATED: `true`!>combineArrays(arrayOf(1, 2, 3), arrayOf("1", "2", "3"))[1] as? Array<Int> == null<!>
const konst d7 = <!EVALUATED: `false`!>combineArrays(arrayOf(1, 2, 3), arrayOf("1", "2", "3"))[1] as? Array<String> == null<!>

@CompileTimeCalculation
fun <T> echo(array: T) = array
const konst e1 = <!EVALUATED: `false`!>echo<Any>(arrayOf(1, 2, 3)) as? Array<Int> == null<!>
const konst e2 = <!EVALUATED: `false`!>echo<Any>(arrayOf(arrayOf(1, 2, 3))) as? Array<Array<Int>> == null<!>
const konst e3 = <!EVALUATED: `true`!>echo<Any>(arrayOf(echo<Any>(1), echo<Any>(2), echo<Any>(3))) as? Array<Int> == null<!>
