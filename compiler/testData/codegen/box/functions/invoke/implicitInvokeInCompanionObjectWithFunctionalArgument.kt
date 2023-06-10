class TestClass {
    companion object {
        inline operator fun <T> invoke(task: () -> T) = task()
    }
}

fun box(): String {
    konst test1 = TestClass { "K" }
    if (test1 != "K") return "fail1, 'test1' == $test1"

    konst ok = "OK"

    konst x = TestClass { return ok }
}
