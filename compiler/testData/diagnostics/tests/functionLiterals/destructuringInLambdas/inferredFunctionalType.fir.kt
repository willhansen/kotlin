// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER
data class A(konst x: Int, konst y: String)
data class B(konst u: Double, konst w: Short)

fun <T> Iterable<T>.foo(x: (T) -> Unit) {}

fun bar(aList: List<A>) {
    aList.foo { (a, b) ->
        a checkType { _<Int>() }
        b checkType { _<String>() }
    }

    aList.foo { (a: Int, b: String) ->
        a checkType { _<Int>() }
        b checkType { _<String>() }
    }

    aList.foo { (a, b): A ->
        a checkType { _<Int>() }
        b checkType { _<String>() }
    }

    aList.foo { (<!COMPONENT_FUNCTION_RETURN_TYPE_MISMATCH!>a: String<!>, b) ->
        a checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Int>() }
        b checkType { _<String>() }
    }

    aList.foo <!ARGUMENT_TYPE_MISMATCH!>{ (a, b): B ->
        b checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><Int>() }
        a checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><String>() }
    }<!>
}
