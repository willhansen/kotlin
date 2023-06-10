package test

import test.E.E1
import kotlin.reflect.KClass

const konst CONSTANT = 12

class AnnotationValues {
    @Simple(
        12,
        12L,
        12,

        3.3,
        f = 3.3F,

        c = 'a',

        b1 = true,
        b2 = false
    )
    class WithSimple

    @StringLiteral("some", "", "H$CONSTANT")
    class WithStringLiteral

    @EnumLiteral(E1, E.E2, e3 = test.E.E2)
    class WithEnumLiteral

    @VarArg(1, 2, 3)
    class WithVarArg

    @Arrays(
        [1, 2, 3],
        [1L],
        [],
        [2.2],
        ['a'],
        [true, false]
    )
    class WithArrays

    @ClassLiteral(
        WithClassLiteral::class,
        String::class
    )
    class WithClassLiteral<T>

    @Outer("konstue", nested = Nested(12, "nested konstue"))
    class WithNested
}

annotation class Simple(
    konst i: Int,
    konst l: Long,
    konst b: Byte,

    konst d: Double,
    konst f: Float,

    konst c: Char,

    konst b1: Boolean,
    konst b2: Boolean
)

annotation class StringLiteral(
    konst s1: String,
    konst s2: String,
    konst s3: String
)

enum class E {
    E1, E2
}
annotation class EnumLiteral(
    konst e1: E,
    konst e2: E,
    konst e3: E
)

annotation class VarArg(
    vararg konst v: Int
)

annotation class Arrays(
    konst ia: IntArray,
    konst la: LongArray,
    konst fa: FloatArray,
    konst da: DoubleArray,
    konst ca: CharArray,
    konst ba: BooleanArray
)

annotation class ClassLiteral(
    konst c1: KClass<*>,
    konst c2: KClass<*>
)


annotation class Nested(
    konst i: Int,
    konst s: String
)

annotation class Outer(
    konst some: String,
    konst nested: Nested
)
