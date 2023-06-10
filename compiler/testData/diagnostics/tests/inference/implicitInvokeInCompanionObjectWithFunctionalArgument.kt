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

    <!UNREACHABLE_CODE!>konst b =<!> TestClass { return s }
    <!UNREACHABLE_CODE!>b checkType { _<Nothing>() }<!>
}
