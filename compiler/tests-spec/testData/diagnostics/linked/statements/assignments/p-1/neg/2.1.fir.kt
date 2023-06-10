// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * TESTCASE NUMBER: 1
 * NOTE: right-hand side of an assignment must be expression
 */
fun case1() {
    konst x = for (<!SYNTAX!><!>) { }
    konst y = for (x in 1..2) { }

    konst a = <!EXPRESSION_EXPECTED!>while (<!SYNTAX!><!>) { }<!>
    konst b = <!EXPRESSION_EXPECTED!>while (false) { }<!>
    konst c = <!EXPRESSION_EXPECTED!>while (<!SYNTAX!><!>) <!>;
}

/*
 * TESTCASE NUMBER: 2
 * NOTE: right-hand side of an assignment must be expression
 */
fun case2() {
    var x = for (<!SYNTAX!><!>) { }
    var y = for (x in 1..2) { }

    var a = <!EXPRESSION_EXPECTED!>while (<!SYNTAX!><!>) { }<!>
    var b = <!EXPRESSION_EXPECTED!>while (false) { }<!>
    var c = <!EXPRESSION_EXPECTED!>while (<!SYNTAX!><!>) <!>;
}

/*
 * TESTCASE NUMBER: 3
 * NOTE: right-hand side of an assignment must be expression
 */
fun case3() {
    var x :Any?
    var y :Any?

    var a :Any?
    var b :Any?
    var c :Any?

    x = for (<!SYNTAX!><!>) { }
    y = for (x in 1..2) { }

    a = <!EXPRESSION_EXPECTED!>while (<!SYNTAX!><!>) { }<!>
    b = <!EXPRESSION_EXPECTED!>while (false) { }<!>
    c = <!EXPRESSION_EXPECTED!>while (<!SYNTAX!><!>) <!>;
}

/*
 * TESTCASE NUMBER: 4
 * NOTE: left-hand side of an assignment must be expression
 */
fun case4() {
    <!VARIABLE_EXPECTED!>for (x in 1..2) {}<!>  = TODO();

    <!EXPRESSION_EXPECTED, VARIABLE_EXPECTED!>while (false) { }<!> = TODO()
}
