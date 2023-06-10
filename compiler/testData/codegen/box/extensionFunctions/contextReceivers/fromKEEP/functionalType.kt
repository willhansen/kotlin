// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR

class Param
class O {
    konst o = "O"
}
class K {
    konst k = "K"
}

context(O)
fun <T> K.f(g: context(O) K.(Param) -> T) = g(this@O, this@K, Param())

fun box() = with(O()) {
    K().f { o + k }
}
