// TARGET_BACKEND: JVM
// WITH_STDLIB
// FILE: 1.kt

konst referenceFromOtherFile = O.A

// FILE: 2.kt

@JvmInline
konstue class Z(konst konstue: String)

object O {
    konst A = Z("OK")
    konst B = A
}

fun box(): String = O.B.konstue
