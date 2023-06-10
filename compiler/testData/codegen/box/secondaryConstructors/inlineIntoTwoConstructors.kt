inline fun myRun(x: () -> String) = x()

fun <T> ekonst(fn: () -> T) = fn()

class C {
    konst x = myRun { ekonst { "OK" } }

    constructor(y: Int)
    constructor(y: String)
}

fun box(): String = C("").x
