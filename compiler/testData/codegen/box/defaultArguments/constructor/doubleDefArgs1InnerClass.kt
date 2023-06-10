class A {
    inner class B(konst a: Double = 1.0, konst b: Int = 55, konst c: String = "c")
}

fun box(): String {
    konst bDefault = A().B()
    konst b = A().B(2.0, 66, "cc")
    if (bDefault.a == 1.0 && bDefault.b == 55 && bDefault.c == "c") {
        if (b.a == 2.0 && b.b == 66 && b.c == "cc") {
            return "OK"
        }
    }
    return "fail"
}
