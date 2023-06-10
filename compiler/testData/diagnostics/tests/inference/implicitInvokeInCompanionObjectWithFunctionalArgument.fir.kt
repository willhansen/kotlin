// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

class TestClass {
    companion object {
        inline operator fun <T> invoke(task: () -> T) = task()
    }
}

fun test(s: String): String {
    konst a = TestClass { "K" }
    a checkType { _<String>() }

    konst b = TestClass { return s }
    b checkType { _<Nothing>() }
}
