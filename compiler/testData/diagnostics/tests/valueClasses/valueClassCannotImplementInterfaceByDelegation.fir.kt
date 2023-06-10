// !SKIP_JAVAC
// !LANGUAGE: +InlineClasses
// ALLOW_KOTLIN_PACKAGE

package kotlin.jvm

annotation class JvmInline

interface IFoo

object FooImpl : IFoo

@JvmInline
konstue class Test1(konst x: Any) : <!VALUE_CLASS_CANNOT_IMPLEMENT_INTERFACE_BY_DELEGATION!>IFoo<!> by FooImpl

@JvmInline
konstue class Test2(konst x: IFoo) : IFoo by x
