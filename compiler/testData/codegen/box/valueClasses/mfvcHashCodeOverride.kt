// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

@JvmInline
konstue class A(konst konstue1: MyClass, konst konstue2: MyClass) {
    override fun hashCode() = 42
}

class MyClass() {
    override fun hashCode() = -1
}

fun box(): String = if (A(MyClass(), MyClass()).hashCode() == 42) "OK" else "Fail"
