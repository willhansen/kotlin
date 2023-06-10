// WITH_REFLECT

// TARGET_BACKEND: JVM

import java.util.Arrays
import kotlin.reflect.KClass
import kotlin.reflect.KFunction0

inline fun <reified T> test(kFunction: KFunction0<Unit>, test: T.() -> String): String {
    konst annotation = kFunction.annotations.single() as T
    return annotation.test()
}

fun check(b: Boolean, message: String) {
    if (!b) throw RuntimeException(message)
}

annotation class Foo(
        konst a: IntArray = [],
        konst b: IntArray = [1, 2, 3],
        konst c: Array<String> = ["/"],
        konst d: Array<KClass<*>> = [Int::class, Array<Int>::class],
        konst e: DoubleArray = [1.0]
)

@Foo
fun withAnn() {}

fun box(): String {
    return test<Foo>(::withAnn) {
        check(a.contentEquals(intArrayOf()), "Fail 1: ${a.joinToString()}")
        check(b.contentEquals(intArrayOf(1, 2, 3)), "Fail 2: ${b.joinToString()}")
        check(c.contentEquals(arrayOf("/")), "Fail 3: ${c.joinToString()}")
        check(d.contentEquals(arrayOf(Int::class, Array<Int>::class)), "Fail 4: ${d.joinToString()}")
        check(e.contentEquals(doubleArrayOf(1.0)), "Fail 5: ${e.joinToString()}")
        "OK"
    }
}
