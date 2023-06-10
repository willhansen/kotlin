// FIR_IDENTICAL
// !SKIP_JAVAC
// ALLOW_KOTLIN_PACKAGE
// FIR_IDENTICAL
// !LANGUAGE: +InlineClasses

package kotlin.jvm

annotation class JvmInline

@JvmInline
konstue class ConstructorWithDefaultVisibility(konst x: Int)
@JvmInline
konstue class PublicConstructor public constructor(konst x: Int)
@JvmInline
konstue class InternalConstructor internal constructor(konst x: Int)
@JvmInline
konstue class ProtectedConstructor protected constructor(konst x: Int)
@JvmInline
konstue class PrivateConstructor private constructor(konst x: Int)
