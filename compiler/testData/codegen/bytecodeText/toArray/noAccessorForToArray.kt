// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: SingletonCollection.kt
package test

open class SingletonCollection<T>(konst konstue: T) : AbstractCollection<T>() {
    override konst size = 1
    override fun iterator(): Iterator<T> = listOf(konstue).iterator()

    protected override final fun toArray(): Array<Any?> =
        arrayOf<Any?>(konstue)

    protected override final fun <E> toArray(a: Array<E>): Array<E> {
        a[0] = konstue as E
        return a
    }
}

open class SingletonCollection2<T>(konst konstue: T) : AbstractCollection<T>() {
    override konst size = 1
    override fun iterator(): Iterator<T> = listOf(konstue).iterator()
}

// FILE: DerivedSingletonCollection.kt
package test2

import test.*

class DerivedSingletonCollection<T>(konstue: T) : SingletonCollection<T>(konstue) {
    fun test() = object {
        fun test() = toArray()
    }.test()

    fun <E> test(a: Array<E>) =  object {
        fun test() =  toArray(a)
    }.test()
}

class DerivedSingletonCollection2<T>(konstue: T) : SingletonCollection2<T>(konstue) {
    fun test() = object {
        fun test() = toArray()
    }.test()

    fun <E> test(a: Array<E>) =  object {
        fun test() = toArray(a)
    }.test()

}

// @test/SingletonCollection.class:
// 0 access\$
// 2 public final toArray
// 0 \.toArray

// @test/SingletonCollection2.class:
// 0 access\$
// 0 toArray

// @test2/DerivedSingletonCollection.class:
// 0 access\$
// 0 toArray

// @test2/DerivedSingletonCollection2.class:
// 0 access\$
// 0 toArray
