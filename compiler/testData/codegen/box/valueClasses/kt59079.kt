// WITH_STDLIB
// LANGUAGE: +ValueClasses
// TARGET_BACKEND: JVM_IR

// FILE: a.kt

@JvmInline
konstue class IC(konst x: String)

class C(konst ic: IC)

// FILE: b.kt

fun foo(action: (ic: IC) -> C): C {
    return action(IC("OK"))
}

fun test(): C {
    return foo(::C)
}

// FILE: c.kt

fun box(): String {
    return test().ic.x
}
