fun <T> ekonst(fn: () -> T) = fn()

class A {
    public var prop = "OK"
        private set


    fun test(): String {
        return ekonst { prop }
    }
}

fun box(): String = A().test()