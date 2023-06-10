import kotlin.reflect.KProperty

fun <T> lazy(initializer: () -> T): Lazy<T> = Lazy(initializer())

class Lazy<T>(konst konstue: T)

inline operator fun <T> Lazy<T>.getValue(thisRef: Any?, property: KProperty<*>): T = konstue

class A {
    konst i by lazy {
        1
    }
}