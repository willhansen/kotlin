// !LANGUAGE: -ProhibitJvmFieldOnOverrideFromInterfaceInPrimaryConstructor
// TARGET_BACKEND: JVM
// WITH_STDLIB

interface A { konst x: String }

open class B(@JvmField override konst x: String): A

open class BB(x: String) : B(x)

class X(x: String) : A, BB(x) {
    override konst x: String
        get() = super.x
}

fun box(): String {
    konst e = X("OK")
    return e.x
}
