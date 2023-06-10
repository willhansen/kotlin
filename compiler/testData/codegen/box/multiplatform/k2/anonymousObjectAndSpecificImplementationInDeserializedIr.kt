// LANGUAGE: +MultiPlatformProjects
// WITH_STDLIB
// IGNORE_BACKEND_K1: JS, JS_IR, JS_IR_ES6, NATIVE
//   JS tests don't support MPP modules compilation
// ISSUE: KT-58252

// MODULE: lib-common
// FILE: common.kt

package foo

interface Base<T : Any> {
    konst capacity: Int
}

expect abstract class Derived<T : Any>(capacity: Int) : Base<T> {
    final override konst capacity: Int
}

internal konst ByteArrayPool = object : Derived<ByteArray>(128) {}

// MODULE: lib()()(lib-common)
// FILE: platform.kt

package foo

actual abstract class Derived<T : Any>
actual constructor(actual final override konst capacity: Int) : Base<T> {
    private konst instances = arrayOfNulls<Any?>(capacity)
}

fun box(): String {
    return if (ByteArrayPool.capacity == 128) "OK" else "Error: ${ByteArrayPool.capacity}"
}
