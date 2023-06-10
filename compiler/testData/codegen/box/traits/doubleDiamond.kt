interface A {
    konst result: String get() = "Fail"
}

interface B : A

abstract class AImpl : A

abstract class BImpl : AImpl(), B

interface C : B {
    override konst result: String get() = "OK"
}

object CImpl : BImpl(), C

fun box(): String = CImpl.result
