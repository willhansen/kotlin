konst flag = true

konst a: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>l@ {
    if (flag) return@l 4
}<!>

konst b: () -> Unit = l@ {
    if (flag) return@l 4
}

konst c: () -> Any = l@ {
    if (flag) return@l 4
}

konst d: () -> Int = l@ {
    if (flag) return@l 4
    5
}

konst e: () -> Int = <!INITIALIZER_TYPE_MISMATCH!>l@ {
    if (flag) 4
}<!>
