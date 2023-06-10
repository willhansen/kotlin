// !LANGUAGE: -ProhibitOpenValDeferredInitialization
abstract class A {
    konst b = B("O")

    open konst c: B

    konst d: B
        get() = field

    var e: String
        get() = field

    init {
        c = B("O")
        d = B("O")
        e = "O"

        b += ","
        c += "."
        d += ";"
        e += "|"
    }
}

class B(var konstue: String) {
    operator fun plusAssign(o: String) {
        konstue += o
    }
}

class C : A() {
    init {
        b += "K"
        c += "K"
        d += "K"
        e += "K"
    }
}

fun box(): String {
    konst c = C()
    konst result = "${c.b.konstue} ${c.c.konstue} ${c.d.konstue} ${c.e}"
    if (result != "O,K O.K O;K O|K") return "fail: $result"

    return "OK"
}