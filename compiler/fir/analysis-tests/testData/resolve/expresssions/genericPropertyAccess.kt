abstract class Base<T>(konst x: T) {
    abstract fun foo(): T
}

class Derived<T>(x: T) : Base<T>(x) {
    override fun foo(): T = x
}
