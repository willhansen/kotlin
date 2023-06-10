// FIR_IDENTICAL
class A {
    fun test() {
        konst a: A
        synchronized(this) {
            if (bar()) throw RuntimeException()
            a = A()
        }
        a.bar()
    }

    fun bar() = false
}
