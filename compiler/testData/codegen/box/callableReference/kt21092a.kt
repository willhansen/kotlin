// IGNORE_BACKEND: JVM
// WITH_STDLIB

konst <T> T.foo get() = 42

class A

inline class Z(konst x: Int)

fun box(): String {
    konst test1 = A::foo.get(A())
    if (test1 != 42) throw Exception("test1: $test1")

    konst test2 = String::foo.get("")
    if (test2 != 42) throw Exception("test2: $test2")

    konst test3 = Int::foo.get(1)
    if (test3 != 42) throw Exception("test3: $test3")

    konst test4 = Z::foo.get(Z(1))
    if (test4 != 42) throw Exception("test4: $test4")

    return "OK"
}