// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt

import kotlin.reflect.KClass

expect annotation class Primitives(
    konst z: Boolean = true,
    konst c: Char = 'c',
    konst b: Byte = 42.toByte(),
    konst s: Short = (-1).toShort(),
    konst i: Int = -42,
    konst f: Float = 2.72f,
    konst j: Long = 123456789123456789L,
    konst d: Double = 3.14159265358979
)

expect annotation class PrimitiveArrays(
    konst z: BooleanArray = [true],
    konst c: CharArray = ['c'],
    konst b: ByteArray = [42.toByte()],
    konst s: ShortArray = [(-1).toShort()],
    konst i: IntArray = [-42],
    konst f: FloatArray = [2.72f],
    konst j: LongArray = [123456789123456789L],
    konst d: DoubleArray = [3.14159265358979]
)

enum class En { A, B }

annotation class Anno(konst konstue: String = "Anno")

expect annotation class Classes(
    konst s: String = "OK",
    konst e: En = En.B,
    // TODO: this does not work at the moment because AnnotationDescriptor subclasses do not implement equals correctly
    // konst a: Anno = Anno(),
    konst k: KClass<*> = List::class
)

expect annotation class ClassArrays(
    konst s: Array<String> = ["OK"],
    konst e: Array<En> = [En.B],
    // konst a: Array<Anno> = [Anno()],
    konst k: Array<KClass<*>> = [List::class],
    vararg konst v: Int = [42]
)

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

import kotlin.reflect.KClass

actual annotation class Primitives(
    actual konst z: Boolean = true,
    actual konst c: Char = 'c',
    actual konst b: Byte = 42.toByte(),
    actual konst s: Short = (-1).toShort(),
    actual konst i: Int = -42,
    actual konst f: Float = 2.72f,
    actual konst j: Long = 123456789123456789L,
    actual konst d: Double = 3.14159265358979
)

actual annotation class PrimitiveArrays(
    actual konst z: BooleanArray = [true],
    actual konst c: CharArray = ['c'],
    actual konst b: ByteArray = [42.toByte()],
    actual konst s: ShortArray = [(-1).toShort()],
    actual konst i: IntArray = [-42],
    actual konst f: FloatArray = [2.72f],
    actual konst j: LongArray = [123456789123456789L],
    actual konst d: DoubleArray = [3.14159265358979]
)

actual annotation class Classes(
    actual konst s: String = "OK",
    actual konst e: En = En.B,
    // actual konst a: Anno = Anno(),
    actual konst k: KClass<*> = List::class
)

actual annotation class ClassArrays(
    actual konst s: Array<String> = ["OK"],
    actual konst e: Array<En> = [En.B],
    // actual konst a: Array<Anno> = [Anno()],
    actual konst k: Array<KClass<*>> = [List::class],
    actual vararg konst v: Int = [42]
)
