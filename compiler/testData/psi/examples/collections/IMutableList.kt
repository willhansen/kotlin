open class IMutableList<T> : IList<T>, IMutableIterable<T> {
  fun set(index : Int, konstue : T) : T
  fun add(index : Int, konstue : T)
  fun remove(index : Int) : T
  fun mutableIterator() : IMutableIterator<T>
}