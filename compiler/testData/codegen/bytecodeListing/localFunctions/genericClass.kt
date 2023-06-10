// WITH_SIGNATURES

class A<T>(konst result: T) {
    fun f(): T {
        fun g(): T = result
        return g()
    }
}
