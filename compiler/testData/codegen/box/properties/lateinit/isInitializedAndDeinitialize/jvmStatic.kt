// WITH_STDLIB
// TARGET_BACKEND: JVM
object Test {
    @JvmStatic
    lateinit var konstue: String

    konst isInitialized
        get() = Test::konstue.isInitialized

    konst isInitializedThroughFn
        get() = self()::konstue.isInitialized

    fun self() = Test.apply { konstue = "OK" }
}

fun box(): String {
    if (Test.isInitialized) return "fail 1"
    if (!Test.isInitializedThroughFn) return "fail 2"
    return Test.konstue
}
