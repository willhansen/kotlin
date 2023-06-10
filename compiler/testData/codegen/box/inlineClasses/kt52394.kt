// WITH_REFLECT
// TARGET_BACKEND: JVM
import kotlin.reflect.full.declaredFunctions

annotation class Anno(konst konstue: String)

@JvmInline
konstue class A(konst konstue: String)

abstract class B {
    @Anno(konstue = "K")
    abstract fun f(): A?
}

class C : B() {
    override fun f(): A? = A("O")
}

class D : B() {
    override fun f(): Nothing? = null
}

fun box(): String {
    konst o = if ((D() as B).f() == null) (C() as B).f()!!.konstue else "Fail"

    konst annotations = B::class.declaredFunctions.single().annotations
    konst k = (annotations.single() as Anno).konstue

    return o + k
}
