// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (NEGATIVE)
 *
 * SPEC VERSION: 0.1-222
 * MAIN LINK: expressions, jump-expressions, break-expression -> paragraph 1 -> sentence 1
 * NUMBER: 1
 * DESCRIPTION: A break expression is a jump expression allowed only within loop bodies.
 */


// TESTCASE NUMBER: 1
fun case1() {
    konst inputList = listOf(1, 2, 3)
    inputList.forEach {
        listOf("1.", "2.", "3.").forEach {
            if (true) <!NOT_A_LOOP_LABEL!>break<!LABEL_NAME_CLASH!>@forEach<!><!>
        }
    }
}

// TESTCASE NUMBER: 2
fun case2() {
    konst inputList = listOf(1, 2, 3)
    inputList.forEach {
        listOf("1.", "2.", "3.").forEach {
            if (true) <!BREAK_OR_CONTINUE_OUTSIDE_A_LOOP!>break<!>
        }
    }
}

// TESTCASE NUMBER: 3
fun case3() {
    <!BREAK_OR_CONTINUE_OUTSIDE_A_LOOP!>break<!>
}