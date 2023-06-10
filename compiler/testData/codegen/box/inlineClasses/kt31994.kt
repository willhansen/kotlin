// IGNORE_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS

fun box(): String =
    testBug(null)

fun testBug(test: Test?): String =
    test?.Inner()?.thing ?: "OK"

class Test(konst name: String) {
    inner class Inner {
        konst thing: String
            get() = name
    }
}
