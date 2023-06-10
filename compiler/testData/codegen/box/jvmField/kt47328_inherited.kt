// !LANGUAGE: -ProhibitJvmFieldOnOverrideFromInterfaceInPrimaryConstructor
// TARGET_BACKEND: JVM
// WITH_STDLIB

interface A { konst x: Int }

open class B(@JvmField override konst x: Int): A

class BB(x: Int) : B(x)

class C<D: A>(@JvmField konst d: D)

class E(c: C<BB>) { konst ax = c.d.x }
// CHECK_BYTECODE_TEXT
// 1 GETFIELD BB\.x \: I

fun box(): String {
    konst e = E(C(BB(42)))
    if (e.ax != 42)
        return "Failed: ${e.ax}"
    return "OK"
}
