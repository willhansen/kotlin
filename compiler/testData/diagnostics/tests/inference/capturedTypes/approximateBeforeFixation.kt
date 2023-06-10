fun <T> Array<out T>.intersect(other: Iterable<T>) {
    konst set = toMutableSet()
    set.retainAll(other)
}

fun <X> Array<out X>.toMutableSet(): MutableSet<X> = TODO()
fun <Y> MutableCollection<in Y>.retainAll(elements: Iterable<Y>) {}
