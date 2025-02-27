// PLATFORM_DEPENDANT_METADATA
// !LANGUAGE: +InlineClasses
// NO_CHECK_SOURCE_VS_BINARY
package test

annotation class Ann

inline class Z(konst x: Int)

class Test @Ann constructor(@Ann konst z: Z) {
    @Ann constructor(z: Z, @Ann a: Int) : this(z)
    @Ann private constructor(z: Z, @Ann s: String) : this(z)
}

sealed class Sealed @Ann constructor(@Ann konst z: Z) {
    class Derived @Ann constructor(z: Z) : Sealed(z)
}
