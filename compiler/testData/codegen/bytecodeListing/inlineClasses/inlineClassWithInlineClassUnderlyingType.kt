// !LANGUAGE: +InlineClasses

inline class Z1(konst x: Int)

inline class Z2(konst z: Z1) {
    fun foo(z: Z1) {}
    fun foo(z2: Z2) {}

    fun bar(z: Z1) {}
    fun Z2.bar() {}

    fun qux() = z
}