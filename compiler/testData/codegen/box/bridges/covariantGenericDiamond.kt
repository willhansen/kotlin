interface A {
    konst result: Any
}
interface B : A {
    override konst result: String
}

abstract class AImpl<out Self : Any>(override konst result: Self) : A
class BImpl(result: String) : AImpl<String>(result), B

fun box(): String = (BImpl("OK") as B).result
