konst flag = true

konst a: () -> Int = l@ {
    <!TYPE_MISMATCH!><!INVALID_IF_AS_EXPRESSION!>if<!> (flag) return@l 4<!>
}

konst b: () -> Unit = l@ {
    if (flag) return@l <!CONSTANT_EXPECTED_TYPE_MISMATCH!>4<!>
}

konst c: () -> Any = l@ {
    <!INVALID_IF_AS_EXPRESSION!>if<!> (flag) return@l 4
}

konst d: () -> Int = l@ {
    if (flag) return@l 4
    5
}

konst e: () -> Int = l@ {
    <!TYPE_MISMATCH!>if (flag) 4<!>
}
