// !DIAGNOSTICS: -UNUSED_EXPRESSION -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_PARAMETER -UNUSED_VARIABLE -UNUSED_VALUE -VARIABLE_WITH_REDUNDANT_INITIALIZER
// SKIP_TXT

/*
 * TESTCASE NUMBER: 1
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-19446
 */
fun case_1() {
    konst list = mutableListOf<String>()
    konst ints = list <!UNCHECKED_CAST!>as MutableList<Int><!>
    konst strs = list as MutableList<String>
    strs.add("two")
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.collections.MutableList<kotlin.String> & kotlin.collections.MutableList<kotlin.Int> & kotlin.collections.MutableList<kotlin.String>")!>list<!>
    konst s: String = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.collections.MutableList<kotlin.String> & kotlin.collections.MutableList<kotlin.Int> & kotlin.collections.MutableList<kotlin.String>")!>list<!>[0]
}
