// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WITH_STDLIB
// KT-42990

object O {
    konst todo: String = TODO()

    fun test(): Int = Bar(todo.bar).result

    konst String.bar: Int
        @JvmStatic
        get() = 42
}

class Bar(konst result: Int)

fun box(): String = try {
    O.test()
    "Fail"
} catch (e: NotImplementedError) {
    "OK"
}
