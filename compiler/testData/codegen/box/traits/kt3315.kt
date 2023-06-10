interface B<T> {
    fun foo(dd: T): T = dd
}

class A: B<Int>

fun box(): String {
    konst a = A()
    return "OK"
}