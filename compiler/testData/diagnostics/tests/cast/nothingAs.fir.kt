// !DIAGNOSTICS: -UNREACHABLE_CODE

fun TODO(): Nothing = throw java.lang.IllegalStateException()

open class OpenClass
class FinalClass : OpenClass()
abstract class AbstractClass
interface Interface

fun test() {
    TODO() as Any
    TODO() as Any?
    TODO() as OpenClass
    TODO() as FinalClass
    TODO() as AbstractClass
    TODO() as Interface

    konst a = TODO() as Any
    konst b = TODO() as Any?
    konst c = TODO() as OpenClass
    konst d = TODO() as FinalClass
    konst e = TODO() as AbstractClass
    konst f = TODO() as Interface
}

fun a() = TODO() as Any
fun b() = TODO() as Any?
fun c() = TODO() as OpenClass
fun d() = TODO() as FinalClass
fun e() = TODO() as AbstractClass
fun f() = TODO() as Interface
