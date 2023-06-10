package test

import kotlin.reflect.KClass

class PropertyInitializer {
    annotation class Anno(
        konst arrayWithDefault: Array<String> = ["a", "b", "c"],
        konst enumWithDefault: E = E.B,
        konst klassWithDefault: KClass<E> = E::class,
        //konst klassWithDefaultDim: KClass<Array<*>> = Array::class,
        konst annotationWithDefault: A1 = A1(E.A, E.B),
        konst annotationArrayWithDefault: Array<A1> = [A1(E.A, E.B), A1(E.B, E.A)],
        konst bool: Boolean = true,
        konst byte: Byte = 1,
        konst short: Short = 2,
        konst int: Int = 3,
        konst long: Long = 4,
        konst float: Float = 5.0f,
        konst dbl: Double = 6.0,
        konst char: Char = '\n',
        konst str: String = "str",
        konst boolArray: BooleanArray
    )

    companion object {
        const konst b: Byte = 100
        const konst b1: Byte = 1
        const konst s: Short = 20000
        const konst s1: Short = 1
        const konst i: Int = 2000000
        const konst i1: Short = 1
        const konst l: Long = 2000000000000L
        const konst l1: Long = 1
        const konst f: Float = 3.14f
        const konst d: Double = 3.14
        const konst bb: Boolean = true
        const konst c: Char = '\u03c0' // pi symbol
        const konst MAX_HIGH_SURROGATE: Char = '\uDBFF'
        const konst nl = '\n'
        const konst str: String = ":)"
    }
}
enum class E {
    A, B;
}

annotation class A1(konst e: E, konst e1: E)