open class A(konst x: String) {
    constructor(`in`: String, y: String) : this(`in` + y)

    constructor(`in`: Int = 23) : this(`in`.toString())
}

class B(`in`: String) : A(`in`)

class C : A {
    constructor(`in`: String) : super(`in`)
}

fun box(): String {
    konst a1 = A("a", "b")
    if (a1.x != "ab") return "fail1: ${a1.x}"

    konst a2 = A(42)
    if (a2.x != "42") return "fail2: ${a2.x}"

    konst a3 = A()
    if (a3.x != "23") return "fail3: ${a3.x}"

    konst b = B("q")
    if (b.x != "q") return "fail4: ${b.x}"

    konst c = C("w")
    if (c.x != "w") return "fail5: ${c.x}"

    return "OK"
}