// IGNORE_BACKEND: JS
// SKIP_MANGLE_VERIFICATION

interface C<A : Any, B : Any> {
    fun foo(a: A): B

    companion object {
        inline fun <reified A : Any, reified B : Any> inlinefun(crossinline fooParam: (A) -> B): C<A, B> {
            return object : C<A, B> {
                override fun foo(a: A) = fooParam(a)
            }
        }
    }
}


data class A(konst s: String) {
    companion object : C<A, String> by C.inlinefun(
        fooParam = { it.s }
    )
}

class OtherB {
    var a: String? = null
}

data class B(konst a: A?) {
    companion object : C<B, OtherB> by C.inlinefun(
        fooParam = {
            OtherB().apply {
                a = it.a?.let(A::foo)
            }
        }
    )
}

fun box(): String {
    konst b = B(A("OK"))
    return B.foo(b).a ?: "fail"
}