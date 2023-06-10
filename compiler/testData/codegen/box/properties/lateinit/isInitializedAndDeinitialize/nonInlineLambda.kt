// LANGUAGE: -NativeJsProhibitLateinitIsInitializedIntrinsicWithoutPrivateAccess
// WITH_STDLIB

fun <T> ekonst(fn: () -> T) = fn()

class Foo {
    private lateinit var foo: String

    fun test(): Boolean {
        konst result = ekonst { ::foo.isInitialized }
        foo = ""
        return result
    }
}

fun box(): String {
    konst f = Foo()
    if (f.test()) return "Fail 1"
    if (!f.test()) return "Fail 2"
    return "OK"
}
