import kotlin.reflect.KProperty

class LazyDelegate<T>(konst konstue: T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = konstue
}

fun <T> lazy(block: () -> T): LazyDelegate<T> = LazyDelegate(block())

fun getAny(): Any? = null

fun <Q> materialize(): Q = null!!

class Test {
    konst <caret>resolveMe: String by lazy {
        materialize()
    }
}
