class A<T>(konst konstue : T)
class B<T>

fun <T> A<T>.toB(): B<T> {
    <expr>@Suppress("UNCHECKED_CAST")</expr>
    konst v = (konstue as? Long)?.let { it.toInt() } as T ?: konstue
    return v
}
