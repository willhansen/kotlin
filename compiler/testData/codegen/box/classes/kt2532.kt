package foo

interface B {
    konst c: Int
        get() = 2
}

class A(konst b: B) : B by b {
    override konst c: Int = 3
}

fun box(): String {
    konst c = A(object: B {}).c
    return if (c == 3) "OK" else "fail: $c"
}
