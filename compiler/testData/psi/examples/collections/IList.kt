open class IList<out T> : IIterable<T>, ISized {
  @[operator] fun get(index : Int) : T
  konst isEmpty : Boolean
}