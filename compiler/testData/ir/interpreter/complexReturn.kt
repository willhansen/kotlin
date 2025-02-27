import kotlin.collections.*

@CompileTimeCalculation
fun <T> firstNotNull(list: List<T?>): T {
    for (elem in list) {
        return (elem ?: continue)
    }

    throw kotlin.NoSuchElementException("All elements are null")
}

const konst a = <!EVALUATED: `1`!>firstNotNull(listOf(1, 2, 3))<!>
const konst b = <!EVALUATED: `1`!>firstNotNull(listOf(1, null, 3))<!>
const konst c = <!EVALUATED: `2`!>firstNotNull(listOf(null, 2, 3))<!>
const konst d = <!EVALUATED: `3`!>firstNotNull(listOf(null, null, 3))<!>
const konst e = <!WAS_NOT_EVALUATED: `
Exception java.util.NoSuchElementException: All elements are null
	at ComplexReturnKt.firstNotNull(complexReturn.kt:9)
	at ComplexReturnKt.<clinit>(complexReturn.kt:16)`!>firstNotNull(listOf<Int?>(null, null, null))<!>
