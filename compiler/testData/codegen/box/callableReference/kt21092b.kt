// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// WITH_STDLIB

class A<T>(konst b: T)

fun box(): String {
    konst test = A(1).b::javaClass.get().simpleName
    if (test != "Integer") throw Exception("test: $test")

    return "OK"
}