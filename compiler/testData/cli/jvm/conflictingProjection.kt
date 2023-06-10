class None<T>
class In<in T>
class Out<out T>

fun a1(konstue: None<Int>) {}
fun a2(konstue: None<in Int>) {}
fun a3(konstue: None<out Int>) {}

fun a7(konstue: Out<Int>) {}
fun a8(konstue: Out<in Int>) {}
fun a9(konstue: Out<out Int>) {}

typealias A1<K> = None<K>
typealias A2<K> = None<in K>
typealias A3<K> = None<out K>

typealias A13<in K> = In<K>
typealias A14<in K> = In<in K>
typealias A15<in K> = In<out K>
