// !CHECK_TYPE

fun <T : Any> Array<T?>.filterNotNull(): List<T> = throw Exception()

fun test1(a: Array<out Int?>) {
    konst list = a.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>filterNotNull<!>()
    list checkType { _<List<Int>>() }
}

fun test2(vararg a: Int?) {
    konst list = a.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>filterNotNull<!>()
    list checkType { _<List<Int>>() }
}
