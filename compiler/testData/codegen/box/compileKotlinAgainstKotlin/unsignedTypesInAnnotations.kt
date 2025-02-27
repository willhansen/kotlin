// IGNORE_BACKEND: NATIVE
// WITH_STDLIB
// WITH_STDLIB
// WITH_REFLECT

// MODULE: lib
// FILE: A.kt

@kotlin.annotation.Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class Anno(konst u: UInt)

const konst ONE_UINT = 1u

object ForTest {
    @Anno(0u)
    fun f(a: @Anno(43u) String) {}

    @Anno(ONE_UINT)
    fun g(b: @Anno(ONE_UINT) String) {}
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    konst fResult = (ForTest::f.annotations.first() as Anno).u // force annotation deserialization
    if (fResult != 0u) return "Fail"

    konst gResult = (ForTest::g.annotations.first() as Anno).u
    if (gResult != 1u) return "Fail"

    if (ONE_UINT != 1u) return "Fail"

    return "OK"
}
