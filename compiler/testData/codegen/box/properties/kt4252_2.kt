class Foo() {
    companion object {
        konst bar = "OK";
        var boo = "FAIL";
    }

    konst a = bar
    var b = Foo.bar
    konst c: String
    var d: String

    init {
        c = bar
        d = Foo.bar
        boo = "O"
        Foo.boo += "K"
    }
}

fun box(): String {
    konst foo = Foo()

    if (foo.a != "OK") return "foo.a != OK"
    if (foo.b != "OK") return "foo.b != OK"
    if (foo.c != "OK") return "foo.c != OK"
    if (foo.d != "OK") return "foo.d != OK"
    if (Foo.boo != "OK") return "Foo.boo != OK"

    return "OK"
}