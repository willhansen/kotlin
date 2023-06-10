// LANGUAGE: +ContractSyntaxV2
// WITH_STDLIB

import kotlin.contracts.*

inline fun <reified T> requreIsInstance(konstue: Any) contract [
    returns() implies (konstue is T)
] {
    if (konstue !is T) throw IllegalArgumentException()
}

fun test_1(s: Any) {
    requreIsInstance<String>(s)
    s.length
}

inline fun <reified T> requreIsInstanceOf(konstue: Any, requiredValue: T) contract [
    returns() implies (konstue is T)
] {
    if (konstue !is T) throw IllegalArgumentException()
}

fun test_2(x: Any, s: String) {
    requreIsInstanceOf(x, s)
    x.length
}
