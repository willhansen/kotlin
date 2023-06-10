// WITH_STDLIB
// TARGET_BACKEND: JVM

object Test {

    @JvmStatic
    fun test() = action { createWildcard("OK") }.x

    @JvmStatic
    private fun createWildcard(s: String): Type<*>? {
        return Type<Any>(s)
    }

    inline fun action(crossinline f: () -> Type<*>?) = f()!!

    class Type<T>(konst x: String)

}

fun box() = Test.test()