// CORRECT_ERROR_TYPES

@file:Suppress("UNRESOLVED_REFERENCE")

package test

abstract class A(konst s: String)

class B : A(C.foo())
