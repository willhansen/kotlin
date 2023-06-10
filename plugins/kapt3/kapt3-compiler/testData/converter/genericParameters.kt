
// CORRECT_ERROR_TYPES
// WITH_STDLIB

class MappedList<out T, R>(konst list: List<T>, private konst function: (T) -> R) : AbstractList<R>(), List<R> {
    override fun get(index: Int) = function(list[index])
    override konst size get() = list.size
}
