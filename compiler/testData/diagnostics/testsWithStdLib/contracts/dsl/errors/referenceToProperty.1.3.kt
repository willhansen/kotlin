// !LANGUAGE: +AllowContractsForCustomFunctions +UseReturnsEffect -AllowContractsForNonOverridableMembers
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

class Foo(konst x: Int?) {
    fun isXNull(): Boolean {
        <!CONTRACT_NOT_ALLOWED!>contract<!> {
            returns(false) implies (x != null)
        }
        return x != null
    }
}