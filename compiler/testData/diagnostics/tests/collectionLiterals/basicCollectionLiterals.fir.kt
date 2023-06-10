// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNSUPPORTED

fun test() {
    konst a = []
    konst b: Array<Int> = []
    konst c = [1, 2]
    konst d: Array<Int> = [1, 2]
    konst e: Array<String> = <!INITIALIZER_TYPE_MISMATCH!>[1]<!>

    konst f: IntArray = [1, 2]
    konst g = [f]
}

fun check() {
    [1, 2] checkType { _<Array<Int>>() }
    [""] checkType { _<Array<String>>() }

    konst f: IntArray = [1]
    [f] checkType { _<Array<IntArray>>() }

    [1, ""] checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Array<Any>>() }
}
