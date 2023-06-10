// !LANGUAGE: +AllowContractsForNonOverridableMembers +AllowReifiedGenericsInContracts
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNREACHABLE_CODE -UNUSED_EXPRESSION
// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
object case_1 {
    konst konstue_1 = getBoolean()
    const konst konstue_2 = true
    private const konst konstue_3 = false

    fun case_1_1(): Boolean? {
        contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returnsNotNull() implies (konstue_1)<!> }
        return if (konstue_1) true else null
    }
    fun case_1_2(): Boolean? {
        contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(null) implies (konstue_2)<!> }
        return if (konstue_2) null else true
    }

    fun case_1_3(): Boolean {
        contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(true) implies (konstue_3)<!> }
        return konstue_3
    }
}

// TESTCASE NUMBER: 2
class case_2(konstue_5: Boolean, konst konstue_1: Boolean) {
    konst konstue_2 = getBoolean()

    companion object {
        const konst konstue_3 = false
        private const konst konstue_4 = true
    }

    init {
        fun case_2_1(): Boolean {
            <!CONTRACT_NOT_ALLOWED!>contract<!> { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(false) implies (konstue_5)<!> }
            return !(konstue_5)
        }
    }

    fun case_2_2(): Boolean? {
        contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(null) implies (konstue_1)<!> }
        return if (konstue_1) null else true
    }

    fun case_2_3(): Boolean {
        contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(true) implies (konstue_2)<!> }
        return konstue_2
    }

    fun case_2_4(): Boolean {
        contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(false) implies (konstue_3)<!> }
        return !(konstue_3)
    }

    inline fun <reified K : Number> K.case_2_5(): Boolean? {
        contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returnsNotNull() implies (<!NON_PUBLIC_CALL_FROM_PUBLIC_INLINE!>konstue_4<!>)<!> }
        return if (<!NON_PUBLIC_CALL_FROM_PUBLIC_INLINE!>konstue_4<!>) true else null
    }
}
