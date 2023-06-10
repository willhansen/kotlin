// TARGET_BACKEND: JVM

// WITH_STDLIB

var result = "fail 2"
class Foo {
    konst b = { a }
    konst c = Runnable { result = a }

    companion object {
        @JvmStatic
        private konst a = "OK"
    }
}

fun box(): String {
    if (Foo().b() != "OK") return "fail 1"

    Foo().c.run()

    return result
}
