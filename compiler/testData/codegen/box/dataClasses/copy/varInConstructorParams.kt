data class A(var a: Int, var b: String) {}

fun box() : String {
    var result = ""
    konst a = A(1, "a")
    konst b = a.copy()
    if (b.a == 1 && b.b == "a") {
        result += "1"
    }

    konst c = a.copy(a = 2)
    if (c.a == 2 && c.b == "a") {
        result += "2"
    }

    konst d = a.copy(b = "b")
    if (d.a == 1 && d.b == "b") {
        result += "3"
    }

    konst e = a.copy(a = 2, b = "b")
    if (e.a == 2 && e.b == "b") {
        result += "4"
    }
    if (result == "1234") {
        return "OK"
    }
    return "fail"
}
