// !DIAGNOSTICS: -UNUSED_VARIABLE
// !LANGUAGE: -ProhibitTypeParametersForLocalVariables

import kotlin.reflect.KProperty

fun test() {
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING!><T><!> a0 = 0
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING!><T : <!DEBUG_INFO_MISSING_UNRESOLVED!>__UNRESOLVED__<!>><!> a1 = ""
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING!><T : <!DEBUG_INFO_MISSING_UNRESOLVED!>String<!>><!> a2 = 0
    <!WRONG_MODIFIER_TARGET!>const<!> konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING!><T><!> a3 = 0
    <!INAPPLICABLE_LATEINIT_MODIFIER, INAPPLICABLE_LATEINIT_MODIFIER, INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING!><T><!> a4 = 0
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING!><T><!> a5 by Delegate<Int>()
    konst <!LOCAL_VARIABLE_WITH_TYPE_PARAMETERS_WARNING!><T><!> a6 by <!DELEGATE_SPECIAL_FUNCTION_NONE_APPLICABLE!>Delegate<<!UNRESOLVED_REFERENCE!>T<!>>()<!>
}

class Delegate<F> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = ""
    operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: String) {}
}
