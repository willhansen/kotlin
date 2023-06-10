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
        a checkType { _<Int>() }
        b checkType { _<String>() }
    }

    y2 checkType { _<(A) -> Unit>() }

    konst z = { <!CANNOT_INFER_PARAMETER_TYPE!>(a: Int, b: String)<!> ->
        <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>a<!> <!DEBUG_INFO_MISSING_UNRESOLVED!>checkType<!> { <!UNRESOLVED_REFERENCE!>_<!><Int>() }
        <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>b<!> <!DEBUG_INFO_MISSING_UNRESOLVED!>checkType<!> { <!UNRESOLVED_REFERENCE!>_<!><String>() }
    }
}
