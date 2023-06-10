// !CHECK_TYPE

fun noCoercionLastExpressionUsedAsReturnArgument() {
    konst a = {
        42
    }

    a checkType { _<() -> Int>() }
}

fun noCoercionBlockHasExplicitType() {
    konst b: () -> Int = {
        <!TYPE_MISMATCH!>if (true) 42<!>
    }
}

fun noCoercionBlockHasExplicitReturn() {
    konst c = l@{
        if (true) return@l 42

        <!INVALID_IF_AS_EXPRESSION!>if<!> (true) 239
    }
}

fun noCoercionInExpressionBody(): Unit = <!TYPE_MISMATCH!>"hello"<!>
