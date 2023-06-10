// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

// FILE: Kt15AbstractMethodError2.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class MyValueClazz(konst base: Long)

fun interface MyInterface {
    fun myMethod(x: MyValueClazz)
}

// FILE: Kt15AbstractMethodErrorRepro.kt

fun box(): String {
    konst foo = MyInterface { _ -> }
    foo.myMethod(MyValueClazz(0L))
    return "OK"
}