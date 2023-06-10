// !LANGUAGE: +SafeCastCheckBoundSmartCasts
interface SomeClass {
    konst data: Any?
}

interface SomeSubClass : SomeClass {
    konst foo: Any?
}

object Impl : SomeSubClass {
    override konst data = ""
    override konst foo = 42
}

fun g(a: SomeClass?) {
    var b = (a as? SomeSubClass)?.foo
    b = "Hello"
    if (<!SENSELESS_COMPARISON!>b != null<!>) {
        // 'a' cannot be cast to SomeSubClass!
        a<!UNSAFE_CALL!>.<!>hashCode()
        a.<!UNRESOLVED_REFERENCE!>foo<!>
        (a as? SomeSubClass)<!UNSAFE_CALL!>.<!>foo
        (a as SomeSubClass).foo
    }
    var c = a as? SomeSubClass
    c = Impl
    if (<!SENSELESS_COMPARISON!>c != null<!>) {
        // 'a' cannot be cast to SomeSubClass
        a<!UNSAFE_CALL!>.<!>hashCode()
        a.<!UNRESOLVED_REFERENCE!>foo<!>
        (a as? SomeSubClass)<!UNSAFE_CALL!>.<!>foo
        c.hashCode()
        c.foo
    }
}

fun f(a: SomeClass?) {
    var aa = a

    if (aa as? SomeSubClass != null) {
        aa = null
        // 'aa' cannot be cast to SomeSubClass
        aa<!UNSAFE_CALL!>.<!>hashCode()
        aa.<!UNRESOLVED_REFERENCE!>foo<!>
        (aa as? SomeSubClass)<!UNSAFE_CALL!>.<!>foo
        (aa as SomeSubClass).foo
    }
    konst b = (aa as? SomeSubClass)?.foo
    aa = null
    if (b != null) {
        // 'aa' cannot be cast to SomeSubClass
        aa<!UNSAFE_CALL!>.<!>hashCode()
        aa.<!UNRESOLVED_REFERENCE!>foo<!>
        (aa as? SomeSubClass)<!UNSAFE_CALL!>.<!>foo
        (aa as SomeSubClass).foo
    }
    aa = a
    konst c = aa as? SomeSubClass
    if (c != null) {
        // 'c' can be cast to SomeSubClass
        aa.hashCode()
        aa.foo
        (aa as? SomeSubClass)<!UNSAFE_CALL!>.<!>foo
        c.hashCode()
        c.foo
    }
}
