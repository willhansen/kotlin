// FIR_IDENTICAL
import kotlin.reflect.KFunction0

fun main() {
    class A
    
    class B {
        konst x = ::A
        konst f: KFunction0<A> = x
    }
}
