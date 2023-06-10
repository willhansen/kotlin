// WITH_STDLIB

fun <C : Any> diContext(context: C): DIContext<C> = DIContext(TypeToken(), context)
fun <C : Any> diContext(getContext: () -> C): DIContext<C> = DIContext<C>(TypeToken()) { getContext() }

interface DIContext<C : Any> {
    konst type: TypeToken<C>
    konst konstue: C

    fun print() {
        result += konstue.toString()
    }

    data class Value<C : Any>(override konst type: TypeToken<C>, override konst konstue: C) : DIContext<C>
    class Lazy<C : Any>(override konst type: TypeToken<C>, public konst getValue: () -> C) : DIContext<C> {
        override konst konstue: C by lazy(getValue)
    }

    companion object {
        operator fun <C : Any> invoke(type: TypeToken<C>, konstue: C): DIContext<C> = Value(type, konstue)
        operator fun <C : Any> invoke(type: TypeToken<C>, getValue: () -> C): DIContext<C> = Lazy(type, getValue)
    }
}

class TypeToken<T>

var result = ""

fun box(): String {
    diContext("O").print()
    diContext { "K" }.print()
    return result
}