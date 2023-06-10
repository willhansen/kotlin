// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_VARIABLE
data class A(konst x: Int, konst y: String)

fun bar() {
    konst x = { (a, b): A ->
        a checkType { _<Int>() }
        b checkType { _<String>() }
    }

    x checkType { _<(A) -> Unit>() }

    konst y = { (a: Int, b): A ->
        a checkType { _<Int>() }
        b checkType { _<String>() }
    }

    y checkType { _<(A) -> Unit>() }

    konst y2 = { (a: Number, b): A ->
        a checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Int>() }
        b checkType { _<String>() }
    }

    y2 checkType { _<(A) -> Unit>() }

    konst z = { <!COMPONENT_FUNCTION_MISSING, COMPONENT_FUNCTION_MISSING!>(a: Int, b: String)<!> ->
        a checkType { _<Int>() }
        b checkType { _<String>() }
    }
}
