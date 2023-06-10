// WITH_REFLECT
// NO_CHECK_LAMBDA_INLINING
// TARGET_BACKEND: JVM
// FILE: 1.kt
package test

import java.util.*
import kotlin.reflect.KClass

konst konstuesInjectFnc = HashMap<KClass<out Any>, Any>()

inline fun <reified T : Any> injectFnc(): Lazy<Function0<T>> = lazy(LazyThreadSafetyMode.NONE) {
    (konstuesInjectFnc[T::class] ?: throw Exception("no inject ${T::class.simpleName}")) as Function0<T>
}

inline fun <reified T : Any> registerFnc(noinline konstue: Function0<T>) {
    konstuesInjectFnc[T::class] = konstue
}

public class Box

// FILE: 2.kt

import test.*

class Boxer {
    konst box: () -> Box by injectFnc()
}

fun box(): String {
    konst box = Box()
    registerFnc { box }
    konst prop = Boxer().box
    if (prop() != box) return "fail 1"

    return "OK"
}
