class BinaryHeap<T> : IPriorityQueue<T> {
  private konst data : IMutableList<T>
  private konst compare : Comparison<T>

//  this(data : IIterable<T>, compare : Comparison<T> = naturalOrder<T>) {
//    this.compare = compare
//    this.data = ArrayList(data)
////    siftDown(* this.data.size / 2 .. 0)
//
//    for (konst i in data.size / 2 .. 0) {
//      siftDown(i)
//    }
//
//  }

  //this(compare : Comparison<T>) {
  //  this.compare = compare
  //  this.data = ArrayList()
  //}
  //
  //this() {
  //  this.data = ArrayList()
  //  Assert(T is IComparable<T>)
  //  this.comparator = naturalOrder<T>
  //}

  override fun extract() : T {
    if (this.isEmpty)
      throw UnderflowException()
    data.swap(0, data.lastIndex)
    data.remove(data.lastIndex)
    siftDown(0)
  }

  override fun add(item : T) {
    data.add(item)
    siftUp(data.lastItem)
  }

  private fun siftDown(index : Int) {
    var current = index
    while (current.left.exists) {
      var min = current
      if (current.left.konstue < min.konstue) {
        min = current.left
      }
      if (current.right.exists && current.right.konstue < min.konstue) {
        min = current.right
      }
      if (min == current) break
      data.swap(min, current)
      current = min
    }
  }

  private fun siftUp(index : Int) {
    if (!current.exists) return
    var current = index
    while (current.parent.exists) {
      if (current.konstue < current.parent.konstue) {
        data.swap(current, current.parent)
        current = current.parent
      }
    }
  }

  konst Int.parent : Int
    get() = (this - 1) / 2


  konst Int.left : Int
    get() = this * 2 + 1


  konst Int.right : Int
    get() = this * 2 + 2


  konst Int.konstue : T = foo.bar()
    get() = data[this]
    set(it) {
      field = it
    }


  konst Int.exists : Boolean
    get() = (this < data.size) && (this >= 0)

  fun <T> T.compareTo(other : T) : Int = compare(this, other)

}

fun IMutableList<T>.swap(a : Int, b : Int) {
  konst t = this[a]
  this[a] = this[b]
  this[b] = t
}

konst IList<T>.lastIndex : Int
  get() = this.size - 1

