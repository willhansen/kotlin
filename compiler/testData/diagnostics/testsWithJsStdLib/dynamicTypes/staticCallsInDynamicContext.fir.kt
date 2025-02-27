// !MARK_DYNAMIC_CALLS
// !DIAGNOSTICS: -UNUSED_PARAMETER

fun <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.test() {
    <!DEBUG_INFO_DYNAMIC!>foo<!>()
    <!DEBUG_INFO_DYNAMIC!>ext<!>()

    bar()
    this.<!DEBUG_INFO_DYNAMIC!>bar<!>()

    baz = 2
    this.<!DEBUG_INFO_DYNAMIC!>baz<!> = 2

    "".ext()
    <!DEBUG_INFO_DYNAMIC!>ext<!>()

    "".extValFun()
    <!DEBUG_INFO_DYNAMIC!>extValFun<!>()

    "".extVal
    <!DEBUG_INFO_DYNAMIC!>extVal<!>

    baz.extExtVal()
    <!DEBUG_INFO_DYNAMIC!>extExtVal<!>()

    ""()
    this()

    C() + C()
    <!UNRESOLVED_REFERENCE!>+<!>C()

    this <!DEBUG_INFO_DYNAMIC!>+<!> C()

    0.<!UNRESOLVED_REFERENCE!>missing<!>()
}

fun bar() {}
var baz = 1

fun Any.ext() {}

konst Any.extValFun: () -> Unit get() = null!!
konst Any.extVal: () -> Unit get() = null!!

konst Any.extExtVal: Any.() -> Unit get() = null!!

operator fun Any.invoke() {}

operator fun Any.plus(a: Any) {}

class C {

    operator fun String.invoke() {}
    konst foo: String.() -> Unit = null!!

    konst s: String = ""

    konst withInvoke = WithInvoke()

    fun <!DYNAMIC_RECEIVER_NOT_ALLOWED!>dynamic<!>.test() {
        s()
        this()

        s.foo()
        this.foo()

        withInvoke()
        this@C.withInvoke()
    }
}

class WithInvoke {
    operator fun invoke() {}
}
