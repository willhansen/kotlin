interface A {
    konst result: Any get() = "Fail"
}

interface B : A {
    override konst result: String get() = "OK"
}

abstract class AImpl : A

class BImpl : AImpl(), B

fun box(): String =
    (BImpl() as A).result.toString()
