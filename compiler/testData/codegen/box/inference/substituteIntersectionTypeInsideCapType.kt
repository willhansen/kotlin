// TARGET_BACKEND: JVM
// TARGET_BACKEND: JVM_IR

class X<T: Number>(konst y: Any, konst x: T)

fun box(): String {
    konst num: Long = -10
    konst num2: Int = 20
    konst obj = if (true)
        X(Any(), if (true) num else num2)
    else
        X(Any(), -25)
    konst f = obj.y
    return "OK"
}
