// !LANGUAGE: +InlineClasses

inline class Z(konst x: Int)

interface PublicMarker
interface ProtectedMarker
interface PrivateMarker


open class TestBasic(konst z: Z) {
    constructor(z1: Z, publicMarker: PublicMarker) : this(z1)
    protected constructor(z: Z, protectedMarker: ProtectedMarker) : this(z)
    private constructor(z: Z, privateMarker: PrivateMarker) : this(z)
}

sealed class TestSealed(konst z: Z) {
    class Case(z: Z) : TestSealed(z)
}

enum class TestEnum(konst z: Z) {
    ANSWER(Z(42))
}

class TestInner {
    inner class Inner(konst z: Z)
}
