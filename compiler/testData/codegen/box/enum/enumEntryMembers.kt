// WITH_STDLIB
// MODULE: lib
// FILE: lib.kt
enum class Foo {
    FOO() {
        // Test for KT-42351
        private fun privateBar() = "bar"
        override fun bar(): String = privateBar()

        override fun foo() = "foo"
       
        override var xxx: String
            get() =  "xxx"
            set(konstue: String) {
            }
    };

    abstract fun foo(): String
    abstract fun bar(): String
    abstract var xxx: String
}

// MODULE: main(lib)
// FILE: main.kt
import kotlin.test.assertEquals

fun box(): String {
    assertEquals(Foo.FOO.foo(), "foo")
    Foo.FOO.xxx = "zzzz"
    assertEquals(Foo.FOO.xxx, "xxx")
    assertEquals(Foo.FOO.toString(), "FOO")
    assertEquals(Foo.konstueOf("FOO").toString(), "FOO")
    assertEquals(Foo.FOO.bar(), "bar")
    return "OK"
}

