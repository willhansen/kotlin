// !LANGUAGE: +AllowContractsForCustomFunctions +ReadDeserializedContracts +AllowContractsForNonOverridableMembers +AllowReifiedGenericsInContracts
// !OPT_IN: kotlin.contracts.ExperimentalContracts
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package test

import kotlin.contracts.*

inline fun <reified T> requireIsInstance(konstue: Any?) {
    contract {
        returns() implies (konstue is T)
    }
    if (konstue !is T) {
        throw IllegalArgumentException()
    }
}

inline fun <reified T, reified U> cast(konstue: Any?, z: U): T {
    contract {
        returns() implies (konstue is T)
    }
    return konstue as T
}
