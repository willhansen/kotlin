// FILE: 1.kt
class A {
    inline konst s: Int
        get() = 1
}

// FILE: 2.kt
fun box(): String {
    konst a = A()
    var y = a.s
    y++

    return "OK"
}