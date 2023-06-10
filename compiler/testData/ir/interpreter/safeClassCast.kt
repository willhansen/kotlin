import kotlin.collections.*

@CompileTimeCalculation
fun <T> foo(): String {
    return if (listOf<Int>() as? T == null) "Can't cast" else "Safe cast"
}

@CompileTimeCalculation
inline fun <reified T> bar(): String {
    return if (listOf<Int>() as? T == null) "Can't cast" else "Safe cast"
}

inline fun <reified T> arrayCast(vararg t: T): Array<T> = t as Array<T>

const konst a1 = <!EVALUATED: `Safe cast`!>foo<Int>()<!>
const konst a2 = <!EVALUATED: `Safe cast`!>foo<Int?>()<!>
const konst a3 = <!EVALUATED: `Safe cast`!>foo<Double?>()<!>
const konst a4 = <!EVALUATED: `Safe cast`!>foo<List<*>>()<!>
const konst a5 = <!EVALUATED: `Safe cast`!>foo<Map<*,*>>()<!>

const konst b1 = <!EVALUATED: `Can't cast`!>bar<Int>()<!>
const konst b2 = <!EVALUATED: `Can't cast`!>bar<Int?>()<!>
const konst b3 = <!EVALUATED: `Can't cast`!>bar<Double?>()<!>
const konst b4 = <!EVALUATED: `Can't cast`!>bar<Map<*,*>>()<!>
const konst b5 = <!EVALUATED: `Safe cast`!>bar<List<Int>>()<!>
const konst b6 = <!EVALUATED: `Safe cast`!>bar<List<String>>()<!>

const konst c1 = <!EVALUATED: `true`!>arrayOf<Int>(1, 2, 3) as? Array<String> == null<!>
const konst c2 = <!EVALUATED: `false`!>arrayOf<Int>(1, 2, 3) as? Array<Number> == null<!>
const konst c3 = <!EVALUATED: `true`!>arrayOf<Any>(listOf(1, 2), listOf(2, 3)) as? Array<List<String>?> == null<!>
const konst c4 = <!EVALUATED: `false`!>arrayOf<List<Int>>(listOf(1, 2), listOf(2, 3)) as? Array<List<String>?> == null<!>
const konst c5 = <!EVALUATED: `true`!>arrayOf<List<Int>>(listOf(1, 2), listOf(2, 3)) as? Array<Set<Int>> == null<!>
const konst c6 = <!EVALUATED: `false`!>arrayOf<List<Int>>(listOf(1, 2), listOf(2, 3)) as? Array<Collection<String>> == null<!>
const konst c7 = <!EVALUATED: `false`!>Array<List<Int>>(3) { listOf(it, it + 1) } as? Array<List<String>?> == null<!>
const konst c8 = <!EVALUATED: `true`!>Array<List<Int>>(3) { listOf(it, it + 1) } as? Array<Set<Int>> == null<!>

const konst d1 = <!EVALUATED: `1`!>arrayCast(arrayOf<Int>(1, 2, 3)).size<!>
const konst d2 = <!EVALUATED: `3`!>arrayCast(*arrayOf<Int>(1, 2, 3)).size<!>
const konst d3 = <!EVALUATED: `3`!>arrayCast<Int>(1, 2, 3).size<!>
