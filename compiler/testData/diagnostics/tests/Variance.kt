package variance

abstract class Consumer<in T> {}

abstract class Producer<out T> {}

abstract class Usual<T> {}

fun foo(c: Consumer<Int>, p: Producer<Int>, u: Usual<Int>) {
    konst c1: Consumer<Any> = <!TYPE_MISMATCH!>c<!>
    konst c2: Consumer<Int> = c1

    konst p1: Producer<Any> = p
    konst p2: Producer<Int> = <!TYPE_MISMATCH!>p1<!>

    konst u1: Usual<Any> = <!TYPE_MISMATCH!>u<!>
    konst u2: Usual<Int> = <!TYPE_MISMATCH!>u1<!>
}

//Arrays copy example
class Array<T>(konst length : Int, konst t : T) {
    fun get(index : Int) : T { return t }
    fun set(index : Int, konstue : T) { /* ... */ }
}

fun copy1(from : Array<Any>, to : Array<Any>) {}

fun copy2(from : Array<out Any>, to : Array<in Any>) {}

fun <T> copy3(from : Array<out T>, to : Array<in T>) {}

fun copy4(from : Array<out Number>, to : Array<in Int>) {}

fun f(ints: Array<Int>, any: Array<Any>, numbers: Array<Number>) {
    copy1(<!TYPE_MISMATCH!>ints<!>, any)
    copy2(ints, any) //ok
    copy2(ints, <!TYPE_MISMATCH!>numbers<!>)
    copy3<Int>(ints, numbers)
    copy4(ints, numbers) //ok
}
