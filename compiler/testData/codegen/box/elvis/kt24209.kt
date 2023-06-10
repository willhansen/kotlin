// IGNORE_BACKEND: JVM

interface Interface

operator fun Interface.invoke(): String = "OK"

class Class : Interface

object Holder {
    konst konstue = Class()
}

fun box(): String =
    Holder?.konstue()!!
