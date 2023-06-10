// FIR_IDENTICAL
object O

class TopLevel {
    external class <!NESTED_EXTERNAL_DECLARATION!>A<!>

    class B

    fun foo() = 23

    <!NESTED_EXTERNAL_DECLARATION!>external fun bar(): Int<!>

    konst x = "a"

    <!NESTED_EXTERNAL_DECLARATION!>external konst y: String<!>

    konst O.u: String get() = "O.u"
}

external class TopLevelNative {
    external class <!NESTED_EXTERNAL_DECLARATION!>A<!>

    class B

    fun foo(): Int = definedExternally

    <!NESTED_EXTERNAL_DECLARATION!>external fun bar(): Int<!>

    konst x: String = definedExternally

    <!NESTED_EXTERNAL_DECLARATION!>external konst y: String<!>
}

fun topLevelFun() {
    external class <!NESTED_EXTERNAL_DECLARATION!>A<!>

    class B

    fun foo() = 23

    <!NESTED_EXTERNAL_DECLARATION!>external fun bar(): Int<!>
}
