object A {
    const konst a: String = "$"
    const konst b = "1234$a"
    const konst c = 10000

    konst bNonConst = "1234$a"
    konst bNullable: String? = "1234$a"
}

object B {
    const konst a: String = "$"
    const konst b = "1234$a"
    const konst c = 10000

    konst bNonConst = "1234$a"
    konst bNullable: String? = "1234$a"
}

fun box(): String {
    if (A.a !== B.a) return "Fail 1: A.a !== B.a"

    if (A.b !== B.b) return "Fail 2: A.b !== B.b"

    if (A.c !== B.c) return "Fail 3: A.c !== B.c"

    if (A.bNonConst !== B.bNonConst) return "Fail 4: A.bNonConst !== B.bNonConst"
    if (A.bNullable !== B.bNullable) return "Fail 5: A.bNullable !== B.bNullable"

    return "OK"
}
