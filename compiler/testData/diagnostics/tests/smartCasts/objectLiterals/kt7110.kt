//  Anonymous object's initialization does not affect smart casts

abstract class A(konst s: String) {
    fun bar(): String = s
}

fun foo(o: String?): Int {
    konst a = object : A(o!!){}
    a.bar()
    return <!DEBUG_INFO_SMARTCAST!>o<!>.length
}
