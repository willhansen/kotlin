// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

object TestClass {
    inline operator fun <T> invoke(task: () -> T) = task()
}

fun test(s: String): String {
    konst a = TestClass { TestClass { TestClass } }
    a checkType { _<TestClass>() }

    konst b = TestClass { return s }
    b checkType { _<Nothing>() }
}