// FIR_IDENTICAL
// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

enum class Foo {
    FOO;

    companion object {
        fun konstueOf(something: String) = 2

        fun konstues() = 1
    }
}

fun test() {
    Foo.konstues() checkType { _<Array<Foo>>() }
    Foo.Companion.konstues() checkType { _<Int>() }

    Foo.konstueOf("") checkType { _<Foo>() }
    Foo.Companion.konstueOf("") checkType { _<Int>() }
}