interface I
class C<T>

private inline fun <reified T> C<T>.f() = object : I {
    konst unused = T::class
}

fun box(): String {
    konst t1 = C<String>().f()
    konst t2 = C<String>().f()
    arrayOf(t1, t2)
    return "OK"
}
