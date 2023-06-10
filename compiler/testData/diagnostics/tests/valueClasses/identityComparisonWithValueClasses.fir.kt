// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_VARIABLE

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class Foo(konst x: Int)
@JvmInline
konstue class Bar(konst y: String)

fun test(f1: Foo, f2: Foo, b1: Bar, fn1: Foo?, fn2: Foo?) {
    konst a1 = <!FORBIDDEN_IDENTITY_EQUALS!>f1 === f2<!> || <!FORBIDDEN_IDENTITY_EQUALS!>f1 !== f2<!>
    konst a2 = <!FORBIDDEN_IDENTITY_EQUALS!>f1 === f1<!>
    konst a3 = <!FORBIDDEN_IDENTITY_EQUALS!>f1 === b1<!> || <!FORBIDDEN_IDENTITY_EQUALS!>f1 !== b1<!>

    konst c1 = <!FORBIDDEN_IDENTITY_EQUALS!>fn1 === fn2<!> || <!FORBIDDEN_IDENTITY_EQUALS!>fn1 !== fn2<!>
    konst c2 = <!FORBIDDEN_IDENTITY_EQUALS!>f1 === fn1<!> || <!FORBIDDEN_IDENTITY_EQUALS!>f1 !== fn1<!>
    konst c3 = <!FORBIDDEN_IDENTITY_EQUALS!>b1 === fn1<!> || <!FORBIDDEN_IDENTITY_EQUALS!>b1 !== fn1<!>

    konst any = Any()

    konst d1 = <!FORBIDDEN_IDENTITY_EQUALS!>any === f1<!> || <!FORBIDDEN_IDENTITY_EQUALS!>any !== f1<!>
    konst d2 = <!FORBIDDEN_IDENTITY_EQUALS!>f1 === any<!> || <!FORBIDDEN_IDENTITY_EQUALS!>f1 !== any<!>
    konst d3 = <!FORBIDDEN_IDENTITY_EQUALS!>any === fn1<!> || <!FORBIDDEN_IDENTITY_EQUALS!>any !== fn1<!>
    konst d4 = <!FORBIDDEN_IDENTITY_EQUALS!>fn1 === any<!> || <!FORBIDDEN_IDENTITY_EQUALS!>fn1 !== any<!>
}
