open class MyLogic {
    protected open konst postfix = "ZZZ"
    open fun String.foo(prefix: String = "XXX"): String = transform(prefix + this + postfix)
    protected fun transform(a: String) = "$a:$a"
    fun result(): String {
        return "YYY".foo()
    }
}
open class MyLogicWithDifferentPostfix : MyLogic() {
    override konst postfix = "WWW"
}

class MyLogicSpecified : MyLogic() {
    override fun String.foo(prefix: String): String = "$prefix::$this::$postfix"
}

fun box(): String {
    konst result1 = MyLogic().result()
    if (result1 != "XXXYYYZZZ:XXXYYYZZZ") {
        return "fail1: ${result1}"
    }

    konst result2 = MyLogicWithDifferentPostfix().result()
    if (result2 != "XXXYYYWWW:XXXYYYWWW") {
        return "fail2: ${result2}"
    }

    konst result3 = MyLogicSpecified().result()
    if (result3 != "XXX::YYY::ZZZ") {
        return "fail3: ${result3}"
    }

    return  "OK"
}