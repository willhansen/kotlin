// IGNORE K2

import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE)
annotation class A(
    konst z: Boolean,
    konst c: Char,
    konst b: Byte,
    konst s: Short,
    konst i: Int,
    konst f: Float,
    konst j: Long,
    konst d: Double,
    konst ui: UInt,
    konst ub: UByte,
    konst us: UShort,
    konst ul: ULong,
    konst ui_max: UInt,
    konst ub_max: UByte,
    konst us_max: UShort,
    konst ul_max: ULong,
    konst za: BooleanArray,
    konst ca: CharArray,
    konst ba: ByteArray,
    konst sa: ShortArray,
    konst ia: IntArray,
    konst fa: FloatArray,
    konst ja: LongArray,
    konst da: DoubleArray,
    konst str: String,
    konst enum: AnnotationTarget,
    konst klass: KClass<*>,
    konst klass2: KClass<*>,
    konst anno: B,
    konst stra: Array<String>,
    konst ka: Array<KClass<*>>,
    konst ea: Array<AnnotationTarget>,
    konst aa: Array<B>
)

annotation class B(konst konstue: String)

@Target(AnnotationTarget.TYPE)
annotation class JvmNamed(@get:JvmName("uglyJvmName") konst konstue: String)

class C {
    fun returnTypeAnnotation(): @A(
        true,
        'x',
        1.toByte(),
        42.toShort(),
        42424242,
        -2.72f,
        239239239239239L,
        3.14,
        1u,
        0xFFu,
        3u,
        4uL,
        0xFFFF_FFFFu,
        UByte.MAX_VALUE,
        0xFF_FFu,
        18446744073709551615u,
        [true],
        ['\''],
        [1.toByte()],
        [42.toShort()],
        [42424242],
        [-2.72f],
        [239239239239239L],
        [3.14],
        "aba\ncaba'\"\t\u0001\u0002\uA66E",
        AnnotationTarget.CLASS,
        C::class,
        IntArray::class,
        B(konstue = "aba\ncaba'\"\t\u0001\u0002\uA66E"),
        ["lmao"],
        [Double::class, Unit::class, LongArray::class, Array<String>::class],
        [AnnotationTarget.TYPEALIAS, AnnotationTarget.FIELD],
        [B("2"), B(konstue = "3")]
    ) Unit {}

    fun parameterTypeAnnotation(p: @JvmNamed("Q_Q") Any): Any = p
}
