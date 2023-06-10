// WITH_STDLIB

fun testFun1(str: String): String {
    konst local = str

    class Local {
        fun foo() = str
    }

    konst list = listOf(0).map { Local() }
    return list[0].foo()
}

fun box(): String {
    return when {
        testFun1("test1") != "test1" -> "Fail #1"
        else -> "OK"
    }
}
