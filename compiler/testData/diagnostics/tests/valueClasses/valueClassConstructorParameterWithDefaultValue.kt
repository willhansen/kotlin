// FIR_IDENTICAL
// ALLOW_KOTLIN_PACKAGE
// !SKIP_JAVAC
// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class Test(konst x: Int = 42)
