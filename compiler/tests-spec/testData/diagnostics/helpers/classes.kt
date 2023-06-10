class Class {
    konst prop_1 = 1
    konst prop_2 = 2
    konst prop_3 = 3
    konst prop_4: Float? = 3f
    konst prop_5: Float = 3f
    konst prop_6: String = "..."
    konst prop_7: Nothing? = null
    konst prop_8: Class? = null
    var prop_9: Boolean = true
    konst prop_10: Number? = 3f
    konst prop_11: Int = 10
    var prop_12: String = ""
    konst prop_13: Any? = ""
    konst prop_14: Comparable<*>? = null
    konst prop_15: Iterable<*>? = null

    fun fun_1(): (Int) -> (Int) -> Int = {number: Int -> { number * 5 }}
    fun fun_2(konstue_1: Int): Int = konstue_1 * 2
    fun fun_3(konstue_1: Int): (Int) -> Int = fun(konstue_2: Int): Int = konstue_1 * konstue_2 * 2
    fun fun_4(): Class? = Class()

    operator fun get(i1: Int, i2: Int) = 10
    operator fun set(i1: Int, i2: Int, el: Int) {}
    operator fun get(i1: Int) = 10
    operator fun set(i1: Int, el: Int) {}
    operator fun invoke() {}
    operator fun invoke(x: Any) = { x: Any -> x }
    operator fun invoke(x: Any, y: Any) {}
    operator fun contains(a: Int) = a > 30
    operator fun contains(a: Long) = a > 30L
    operator fun contains(a: Char) = a > 30.toChar()

    fun getIntArray() = intArrayOf(1, 2, 3, 4, 5)
    fun getLongArray() = longArrayOf(1L, 2L, 3L, 4L, 5L)
    fun getCharArray() = charArrayOf(1.toChar(), 2.toChar(), 3.toChar(), 4.toChar(), 5.toChar())

    class NestedClass {
        konst prop_4 = 4
        konst prop_5 = 5
    }
}

operator fun Class?.inc(): Class? = null
operator fun Class?.dec(): Class? = null
operator fun Class?.plus(x: Class?): Class? = null
operator fun Class?.minus(x: Class?): Class? = null

open class ClassWithCustomEquals {
    override fun equals(other: Any?) = true
}

open class ClassWithCostructorParam(konst x: Any)
open class ClassWithCostructorTwoParams(konst x: Any, konst y: Any)

class EmptyClass {}

class ClassWithCompanionObject {
    companion object {}
}

open class ClassLevel1 {
    fun test1() {}
}
open class ClassLevel2: ClassLevel1() {
    fun test2() {}
}
open class ClassLevel21: ClassLevel1() {
    fun test21() {}
}
open class ClassLevel22: ClassLevel1() {
    fun test22() {}
}
open class ClassLevel23: ClassLevel1() {
    fun test23() {}
}
open class ClassLevel3: ClassLevel2() {
    fun test3() {}
}
open class ClassLevel4: ClassLevel3() {
    fun test4() {}
}
open class ClassLevel5: ClassLevel4() {
    fun test5() {}
}
class ClassLevel6: ClassLevel5() {
    fun test6() {}
}

class Inv<T>(konst x: T = null as T) {
    konst prop_1: Inv<T>? = null
    konst prop_2: T? = null
    konst prop_3: T = null as T
    konst prop_4 = 10

    fun test() {}
    fun get() = x
    fun put(x: T) {}
    fun getNullable(): T? = if (true) x else null
}

class In<in T>() {
    fun put(x: T) {}
    fun <K : T> getWithUpperBoundT(x: T): K = x as K
}

class Out<out T>(konst x: T = null as T) {
    konst prop_1: Inv<out T>? = null
    konst prop_2: T? = null

    fun get() = x
}

open class ClassWithTwoTypeParameters<K, L> {
    fun test1(): L? { return null }
    fun test2(): K? { return null }
}

open class ClassWithThreeTypeParameters<K, L, M>(
    konst x: K,
    konst y: L,
    konst z: M
)

open class ClassWithSixTypeParameters<K, in L, out M, O, in P, out R>(
    konst u: R,
    konst x: K,
    konst y: M,
    konst z: O
) {
    fun test() = 10
}
