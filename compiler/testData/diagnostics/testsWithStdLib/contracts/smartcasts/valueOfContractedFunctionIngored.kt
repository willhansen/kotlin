// FIR_IDENTICAL
// !LANGUAGE: +AllowContractsForCustomFunctions +UseReturnsEffect
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

fun f3(konstue: String?) {
    if (<!USELESS_IS_CHECK!>!konstue.isNullOrEmpty() is Boolean<!>) {
        konstue<!UNSAFE_CALL!>.<!>length
    }
}