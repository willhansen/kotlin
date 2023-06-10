// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// CHECK_BYTECODE_LISTING

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst konstue: MyClass) {
    override fun hashCode() = 42
}

class MyClass() {
    override fun hashCode() = -1
}

fun box(): String = if (A(MyClass()).hashCode() == 42) "OK" else "Fail"
