// FIR_IDENTICAL
fun <R : Any> unescape(konstue: Any): R? = throw Exception("$konstue")

fun <T: Any> foo(v: Any): T? = unescape(v)

//--------------

interface A

fun <R : A> unescapeA(konstue: Any): R? = throw Exception("$konstue")


fun <T: A> fooA(v: Any): T? = unescapeA(v)

