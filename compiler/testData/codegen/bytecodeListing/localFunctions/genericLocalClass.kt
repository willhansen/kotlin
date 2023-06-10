// WITH_SIGNATURES

class A<T>(konst result: T) {
    fun b() {
        class C<S> {
            fun f() {
                fun g(t: T): S? = null
            }
        }
    }
}
