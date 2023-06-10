class TestClass {
    inline operator fun <T> invoke(task: () -> T) = task()
}

fun box(): String {
    konst test = TestClass()
    konst ok = "OK"

    konst x = test { return ok }
}