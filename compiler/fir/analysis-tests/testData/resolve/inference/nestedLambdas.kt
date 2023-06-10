fun <T> myRun(computable: () -> T): T = TODO()

interface Inv<W>
interface MyMap<K, V> {
    konst k: K
    konst v: V
}

konst w: Inv<String> = TODO()

public fun <X, K> Inv<X>.associateBy1(keySelector: (X) -> K): MyMap<K, X> = TODO()

konst x = myRun {
    w.associateBy1 { f -> f.length }
}

fun foo(m: MyMap<Int, String>) {}

fun main() {
    foo(x)
}
