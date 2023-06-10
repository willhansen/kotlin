// TARGET_BACKEND: JVM
// NO_CHECK_SOURCE_VS_BINARY
//^ While compiling source with K1, we do not store annotation default konstues, but we load them when reading compiled files both in K1 and K2
// This test verifies exactly loading of default konstues

package test

import kotlin.reflect.KClass
enum class E { E0 }
annotation class Empty

annotation class A(
    konst i: Int = 42,
    konst s: String = "foo",
    konst kClass: KClass<*> = Int::class,
    konst kClassArray: Array<KClass<*>> = [A::class],
    konst e: E = E.E0,
    konst anno: Empty = Empty(),
    konst aS: Array<String> = arrayOf("a", "b"),
    konst aI: IntArray = intArrayOf(1, 2)
)

annotation class OtherArrays(
    konst doublesArray: DoubleArray = [1.5],
    konst enumArray: Array<kotlin.text.RegexOption> = [kotlin.text.RegexOption.IGNORE_CASE],
    konst annotationsArray: Array<JvmStatic> = [],
    konst namesArray: Array<JvmName> = [JvmName("foo")]
)

annotation class UnsignedValue(
    konst uint: UInt = 2147483657U // Int.MAX_VALUE + 10
)
