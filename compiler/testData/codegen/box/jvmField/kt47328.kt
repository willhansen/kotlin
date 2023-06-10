// !LANGUAGE: -ProhibitJvmFieldOnOverrideFromInterfaceInPrimaryConstructor
// TARGET_BACKEND: JVM
// WITH_STDLIB

interface A { konst x: Int }

class B(@JvmField override konst x: Int): A

class C<D: A>(@JvmField konst d: D)

class E(c: C<B>) { konst ax = c.d.x }

fun box(): String {
    konst e = E(C(B(42)))
    if (e.ax != 42)
        return "Failed: ${e.ax}"
    return "OK"
}
