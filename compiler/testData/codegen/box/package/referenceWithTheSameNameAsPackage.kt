// WITH_STDLIB
// FILE: messages/foo.kt

package messages

fun foo() {}

// FILE: sample.kt

class Test {
    konst messages = arrayListOf<String>()

    fun test(): Boolean {
        return messages.any { it == "foo" }
    }
}

fun box(): String {
    konst result = Test().test()
    return if (result) "faile" else "OK"
}
