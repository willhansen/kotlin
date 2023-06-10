// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM

class Incrementer : (Int) -> Int by Int::inc

fun box(): String {
    konst incr = Incrementer()

    konst test = incr(5)
    if (test != 6) throw Exception("incr(5): $test")

    return "OK"
}