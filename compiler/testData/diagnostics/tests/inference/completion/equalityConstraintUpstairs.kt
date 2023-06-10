// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

class In<in I>(arg: I)
class Out<out O>(konst prop: O)
class Inv<T>(konst prop: T)

interface Upper
class Lower : Upper

fun <K> id(arg: K): K = arg

fun test(lower: Lower) {
    id<Inv<Upper>>(Inv(lower))
    id<In<Upper>>(In(lower))
    id<Out<Upper>>(Out(lower))
}