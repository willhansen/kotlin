// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

interface IFoo

object FooImpl : IFoo

inline class Test1(konst x: Any) : <!VALUE_CLASS_CANNOT_IMPLEMENT_INTERFACE_BY_DELEGATION!>IFoo by FooImpl<!>

inline class Test2(konst x: IFoo) : IFoo by x
