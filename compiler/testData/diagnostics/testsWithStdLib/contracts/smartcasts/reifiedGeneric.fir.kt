// !LANGUAGE: +AllowContractsForCustomFunctions +UseReturnsEffect +AllowReifiedGenericsInContracts
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER -UNUSED_VARIABLE

import kotlin.contracts.*

inline fun <reified T> requireIsInstance(konstue: Any?) {
    contract {
        returns() implies (konstue is T)
    }
    if (konstue !is T) {
        throw IllegalArgumentException()
    }
}

inline fun <reified T> cast(konstue: Any?): T {
    contract {
        returns() implies (konstue is T)
    }
    return konstue as T
}

fun test_1(x: Any) {
    requireIsInstance<String>(x)
    x.length
}

fun test_2(x: Any) {
    konst s: String = cast(x)
    x.length
}
