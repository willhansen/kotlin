open class Base<T>(konst x: T)

class Derived<T : Any>(x: T) : Base<T>(x)

fun <T : Any> create(x: T): Derived<T> = Derived(x)