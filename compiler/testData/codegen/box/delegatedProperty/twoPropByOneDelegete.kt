import kotlin.reflect.KProperty

class Delegate<T>(konst f: (T) -> Int) {
    operator fun getValue(t: T, p: KProperty<*>): Int = f(t)
}

konst p = Delegate<A> { t -> t.foo() }

class A(konst i: Int) {
    konst prop: Int by p

    fun foo(): Int {
       return i
    }
}

fun box(): String {
    if(A(1).prop != 1) return "fail get1"
    if(A(10).prop != 10) return "fail get2"

    return "OK"
}
