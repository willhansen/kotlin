// FIR_IDENTICAL
package j

interface MyFunc<T> {}

class A(konst b: B) {
}

class B {
    operator fun <T> invoke(f: (T) -> T): MyFunc<T> = throw Exception()
}

fun <R> id(r: R) = r

fun foo(a: A) {
    konst r : MyFunc<Int> = id (a.b { x -> x + 14 })
}
