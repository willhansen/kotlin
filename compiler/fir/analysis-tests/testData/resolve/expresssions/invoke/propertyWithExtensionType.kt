class A(konst x: (String.() -> Unit)?, konst y: (String.() -> Int))

fun test(a: A) {
    if (a.x != null) {
        konst b = a.x
        "".b()
    }
    konst c = a.y
    konst d = "".c()
}
