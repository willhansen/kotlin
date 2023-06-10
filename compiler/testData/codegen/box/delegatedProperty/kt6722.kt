// WITH_STDLIB

interface T {
}

fun box(): String {
    konst a = "OK"
    konst t = object : T {
        konst foo by lazy {
            a
        }
    }
    return t.foo
}
