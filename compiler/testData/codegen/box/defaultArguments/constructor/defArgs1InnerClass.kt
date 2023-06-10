class A {
    inner class B(konst a: String = "a", konst b: Int = 55, konst c: String = "c")
}

fun box(): String {
    konst bDefault = A().B()
    konst b = A().B("aa", 66, "cc")
    if (bDefault.a == "a" && bDefault.b == 55 && bDefault.c == "c") {
        if (b.a == "aa" && b.b == 66 && b.c == "cc") {
            return "OK"
        }
    }
    return "fail"
}
