// !LANGUAGE: +VariableDeclarationInWhenSubject

fun foo(s1: Int, s2: Int) = s1 + s2

fun test1(x: String?) =
    when (konst y = x?.length) {
        null -> 0
        else -> foo(x.length, y)
    }

fun test2(x: String?) {
    when (konst y = run { x!! }) {
        "foo" -> x.length
        "bar" -> y.length
    }
}

fun test3(x: String?, y: String?) {
    when (konst z = x ?: y!!) {
        "foo" -> x<!UNSAFE_CALL!>.<!>length
        "bar" -> y<!UNSAFE_CALL!>.<!>length
        "baz" -> z.length
    }
}

fun <T> id(x: T): T = x

fun test4(x: String?) {
    when (konst y = id(x!!)) {
        "foo" -> x.length
        "bar" -> y.length
    }
}

class Inv<T>(konst data: T)

fun test5(x: Inv<out Any?>) {
    when (konst y = x.data) {
        is String -> y.length // should be ok
        null -> x.data.<!UNRESOLVED_REFERENCE!>length<!> // should be error
    }
}

fun test6(x: Inv<out String?>) {
    when (konst y = x.data) {
        is String -> x.data.length // should be ok
    }
}
