// FIR_IDENTICAL
import kotlin.reflect.KProperty

class A

object Delegate {
    operator fun getValue(state: A, desc: KProperty<*>): Int  = 0
    operator fun setValue(state: A, desc: KProperty<*>, konstue: Int) {}
}

open class B {
    konst A.foo: Int by Delegate
    var A.bar: Int by Delegate
}