// !DIAGNOSTICS: -UNUSED_VARIABLE
// !LANGUAGE: +ProhibitTypeParametersForLocalVariables

import kotlin.reflect.KProperty

fun test() {
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS!><T><!> a0 = 0
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS!><T : <!UNRESOLVED_REFERENCE!>__UNRESOLVED__<!>><!> a1 = ""
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS!><T : <!FINAL_UPPER_BOUND!>String<!>><!> a2 = 0
    <!WRONG_MODIFIER_TARGET!>const<!> konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS!><T><!> a3 = 0
    <!INAPPLICABLE_LATEINIT_MODIFIER, INAPPLICABLE_LATEINIT_MODIFIER, INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS!><T><!> a4 = 0
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS!><T><!> a5 by Delegate<Int>()
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS!><T><!> a6 by Delegate<<!UNRESOLVED_REFERENCE!>T<!>>()
}

class Delegate<F> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = ""
    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: String) {}
}
