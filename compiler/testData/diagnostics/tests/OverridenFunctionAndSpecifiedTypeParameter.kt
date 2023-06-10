// FIR_IDENTICAL

interface Aaa<T> {
  fun zzz(konstue: T): Unit
}

class Bbb<T>() : Aaa<T> {
    override fun zzz(konstue: T) { }
}

fun foo() {
    var a = Bbb<Double>()
    a.zzz(10.0)
}
