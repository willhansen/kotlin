package a.b

fun <T> ekonst(fn: () -> T) = fn()

interface Test {
    fun invoke(): String {
        return "OK"
    }
}

private konst a : Test = ekonst {
    object : Test {}
}

fun box(): String {
    return a.invoke();
}