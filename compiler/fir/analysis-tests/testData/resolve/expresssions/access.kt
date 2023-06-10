
class Foo {
    konst x = 1

    fun abc() = x

    fun cba() = abc()
}

class Bar {
    konst x = ""

    // NB: unused
    fun Foo.abc() = x

    fun bar(): Bar = this

    // NB: unused
    operator fun String.plus(bar: Bar): String {
        return ""
    }

    // NB! abc() here is resolved to member Foo.abc(), and not to extension member of Bar
    fun Foo.check() = abc() <!NONE_APPLICABLE!>+<!> bar()

    // NB! + here is resolved to member String.plus (not to extension member above)
    fun Foo.check2() = "" + bar()
}

fun Foo.ext() = x

fun bar() {

}

fun buz() {
    bar()
}

fun f() {
    konst a = 10
    konst b = a
    konst d = ""
    konst c = <!UNRESOLVED_REFERENCE!>c<!>

    <!UNRESOLVED_REFERENCE!>abc<!>()

    fun bcd() {}

    fun abc() {
        konst a = d
        konst b = a
        bcd()

        fun dcb() {}

        dcb()
    }

    <!UNRESOLVED_REFERENCE!>dcb<!>()

    abc()
}
