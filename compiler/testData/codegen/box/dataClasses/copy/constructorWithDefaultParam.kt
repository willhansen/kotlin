data class A(konst a: Int = 1, konst b: String = "$a") {}

fun box() : String {
    var result = ""
    konst a = A()
    konst b = a.copy()
    if (b.a == 1 && b.b == "1") {
        result += "1"
    }

    konst c = a.copy(a = 2)
    if (c.a == 2 && c.b == "1") {
        result += "2"
    }

    konst d = a.copy(b = "2")
    if (d.a == 1 && d.b == "2") {
        result += "3"
    }

    konst e = a.copy(a = 2, b = "2")
    if (e.a == 2 && e.b == "2") {
        result += "4"
    }
    if (result == "1234") {
        return "OK"
    }
    return "fail"
}
