// FIR_IDENTICAL
// ALLOW_KOTLIN_PACKAGE
// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses

package kotlin.jvm

annotation class JvmInline

abstract class AbstractBaseClass

open class OpenBaseClass

interface BaseInterface

@JvmInline
konstue class TestExtendsAbstractClass(konst x: Int) : <!VALUE_CLASS_CANNOT_EXTEND_CLASSES!>AbstractBaseClass<!>()

@JvmInline
konstue class TestExtendsOpenClass(konst x: Int) : <!VALUE_CLASS_CANNOT_EXTEND_CLASSES!>OpenBaseClass<!>()

@JvmInline
konstue class TestImplementsInterface(konst x: Int) : BaseInterface
