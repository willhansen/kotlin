class PriorityQueueAsPushPop<T>(wrapped : IPriorityQueue<T>) : IPushPop<T> {
  override fun pop() = wrapped.extract()
  override fun push(item : T) = wrapped.add(item)
  override konst isEmpty
    get() = wrapped.isEmpty

}