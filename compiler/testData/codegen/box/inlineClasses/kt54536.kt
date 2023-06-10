// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR

@JvmInline
konstue class A(konst x: Int) {
    operator fun equals(other: A) = x % 5 == other.x % 5
}

@JvmInline
konstue class B(konst x: A)

fun box() = if (B(A(0)) == B(A(5))) "OK" else "Fail"

// CHECK_BYTECODE_TEXT
// 0 INVOKESTATIC B.box-impl
// 0 INVOKESTATIC A.box-impl