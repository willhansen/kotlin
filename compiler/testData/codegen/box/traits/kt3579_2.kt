interface First
interface Some<T : First> where T : Some<T>

konst a: Some<*>? = null

class MClass(konst p: String) : First, Some<MClass>

fun box(): String {
    return MClass("OK").p
}
