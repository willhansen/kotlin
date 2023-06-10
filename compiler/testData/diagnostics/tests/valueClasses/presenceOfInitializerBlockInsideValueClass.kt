// FIR_IDENTICAL
// !SKIP_JAVAC
// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_VARIABLE

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class Foo(konst x: Int) {
    init {}

    init {
        konst f = 1
    }
}
