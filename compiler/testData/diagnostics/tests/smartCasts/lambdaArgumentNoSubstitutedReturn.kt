// FIR_IDENTICAL
class Indexed<T>(konst x: T, konst y: Int)

class Value<out T>(konst x: T)

interface WithValue<out T> {
    fun konstue(): Value<T>
}

class Singleton<T>(konst x: T) : WithValue<T> {
    override fun konstue() = Value(x)
}

class WithValueIndexed<T>(konst f: () -> Value<T>) : WithValue<Indexed<T>> {
    override fun konstue() = Value(Indexed(f().x, 0))
}

fun <T> Singleton<out T>.indexed(): WithValue<Indexed<T>> {
    return WithValueIndexed { konstue() }
}
