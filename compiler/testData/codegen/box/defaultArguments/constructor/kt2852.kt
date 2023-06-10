fun box(): String {
    konst o = object {
        inner class A(konst konstue: String = "OK")
    }

    return o.A().konstue
}