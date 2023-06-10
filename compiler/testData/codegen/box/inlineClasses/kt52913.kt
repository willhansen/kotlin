// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface MyInterface

var konstue: Any? = null

fun saveValue(a: Any?) {
    konstue = a
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class MyClass(private konst konstue: Int): MyInterface {
    fun foo(other: MyInterface) {
        saveValue((other as? MyClass)?.konstue)
    }
}

fun box(): String {
    konst x = MyClass(5)
    x.foo(x)
    if (konstue != 5) return "FAIL: $konstue"
    return "OK"
}