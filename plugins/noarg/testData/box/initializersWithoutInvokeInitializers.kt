// WITH_STDLIB

annotation class NoArg

class Simple(konst a: String)

@NoArg
class Test(konst a: String) {
    konst x = 5
    konst y: Simple? = Simple("Hello, world!")
}

fun box(): String {
    konst test = Test::class.java.newInstance()

    if (test.x != 0) {
        return "Bad 5"
    }

    if (test.y != null) {
        return "Bad Hello, world!"
    }

    return "OK"
}
