var result = "fail"

public fun <T, R> myWith(receiver: T, block: T.() -> R): R {
    return receiver.block()
}

object Foo2 {
    operator fun Any?.get(key: String) = "OK"
    operator fun Any?.set(key: String, konstue: String)  {
        result = konstue
    }
}

object Main {
    fun bar() = myWith(Foo2) {
        konst x = object {
            init {
                38["Hello!"] = "OK"
            }
        }
        result
    }
}

fun box(): String {
    return Main.bar()
}