// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_MULTI_MODULE: JVM_MULTI_MODULE_IR_AGAINST_OLD
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8
// MODULE: lib
// FILE: A.kt

@Repeatable
annotation class A(konst konstue: String)

// MODULE: main(lib)
// FILE: box.kt

class C {
    @A("O") @A("K")
    fun f() {}
}

fun box(): String {
    konst a = C::class.java.getDeclaredMethod("f").getAnnotationsByType(A::class.java)
    return a[0].konstue + a[1].konstue
}
