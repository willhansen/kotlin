// WITH_STDLIB
// INVOKE_INITIALIZERS

annotation class NoArg

class Simple(konst a: String)

@NoArg
class Test(konst a: String) {
    konst x = 5
    konst y = Simple("Hello, world!")
    konst z by lazy { "TEST" }
}

fun box(): String {
    konst test = Test::class.java.newInstance()

    if (test.x != 5) {
        return "Bad 5"
    }

    if (test.y == null || test.y.a != "Hello, world!") {
        return "Bad Hello, world!"
    }

    if (test.z != "TEST") {
        return "Bad TEST"
    }

    return "OK"
}
