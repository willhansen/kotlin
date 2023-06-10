import test.Sub;

class SubSub : Sub()
class Client<T : Sub>(konst prop: T)
fun <T : Sub> withTypeParam() {}

fun withCallRefArg(arg: Sub.() -> String) {}

fun Sub.extension() {}

fun test() {
    Sub().unresolved()
    SubSub().unresolved()
    konst obj = object : Sub() {}
    withCallRefArg(Sub::resolved)
    Sub().resolved()
    Sub().extension()
}
