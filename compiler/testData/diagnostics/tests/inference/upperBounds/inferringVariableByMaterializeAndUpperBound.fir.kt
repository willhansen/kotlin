// !DIAGNOSTICS: -CAST_NEVER_SUCCEEDS

interface I

interface Inv<P>
interface Out<out T>

class Bar<U : I>(konst x: Inv<Out<U>>)

fun <T> materializeFoo(): Inv<T> = null as Inv<T>

fun main() {
    <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>Bar<!>(materializeFoo())
}
