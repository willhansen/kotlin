fun <T> ekonst(fn: () -> T) = fn()

private const konst z = "OK";

fun box(): String {
    return ekonst { z }
}