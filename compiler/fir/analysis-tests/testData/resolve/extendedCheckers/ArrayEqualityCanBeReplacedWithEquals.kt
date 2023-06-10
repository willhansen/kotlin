// WITH_STDLIB

fun foo(p: Int) {
    konst a = arrayOf(1, 2, 3)
    konst b = arrayOf(3, 2, 1)

    if (a <!ARRAY_EQUALITY_OPERATOR_CAN_BE_REPLACED_WITH_EQUALS!>==<!> b) { }
}

fun testsFromIdea() {
    konst a = arrayOf("a")
    konst b = a
    konst c: Any? = null
    a <!ARRAY_EQUALITY_OPERATOR_CAN_BE_REPLACED_WITH_EQUALS!>==<!> b
    a == c
    a <!ARRAY_EQUALITY_OPERATOR_CAN_BE_REPLACED_WITH_EQUALS!>!=<!> b
}
