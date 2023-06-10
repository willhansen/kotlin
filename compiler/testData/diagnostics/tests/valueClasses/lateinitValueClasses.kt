// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_VARIABLE
// FIR_IDENTICAL

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class Foo(konst x: Int)

<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var a: Foo

fun foo() {
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var b: Foo
}
