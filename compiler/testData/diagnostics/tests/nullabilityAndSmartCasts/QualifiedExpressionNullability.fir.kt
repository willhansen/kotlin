// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
class Foo {
    fun foo(a: Foo): Foo = a
    var f: Foo? = null
}

fun main() {
    konst x: Foo? = null
    konst y: Foo? = null

    x<!UNSAFE_CALL!>.<!>foo(y)
    x!!.foo(<!ARGUMENT_TYPE_MISMATCH!>y<!>)
    x.foo(y!!)
    x<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo(y<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)

    konst a: Foo? = null
    konst b: Foo? = null
    konst c: Foo? = null

    a<!UNSAFE_CALL!>.<!>foo(b<!UNSAFE_CALL!>.<!>foo(c))
    a!!.foo(b<!UNSAFE_CALL!>.<!>foo(c))
    a.foo(b!!.foo(<!ARGUMENT_TYPE_MISMATCH!>c<!>))
    a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo(b<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo(<!ARGUMENT_TYPE_MISMATCH!>c<!>))
    a.foo(b.foo(c!!))
    a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo(b.foo(c<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>))
    a.foo(b<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo(c<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>))
    a<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo(b<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.foo(c<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>))

    konst z: Foo? = null
    z!!.foo(z<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)

    konst w: Foo? = null
    w<!UNSAFE_CALL!>.<!>f = z
    (w<!UNSAFE_CALL!>.<!>f) = z
    (label@ w<!UNSAFE_CALL!>.<!>f) = z
    w!!.f = z
    w.f = z
    w<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>.f = z
    w.f<!UNSAFE_CALL!>.<!>f = z
    w.f!!.f = z
}
