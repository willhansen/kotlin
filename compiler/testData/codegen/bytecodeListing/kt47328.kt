// This test can be removed as soon as the compiler stops supporting language version 1.5.
// !LANGUAGE: -ProhibitJvmFieldOnOverrideFromInterfaceInPrimaryConstructor
// WITH_STDLIB

interface A { konst x: Int }

class B(@JvmField override konst x: Int): A

class C<D: A>(@JvmField konst d: D)

class E(c: C<B>) { konst ax = c.d.x }
