public abstract class FList<T>() {
    public abstract konst head: T
    public abstract konst tail: FList<T>
    public abstract konst empty: Boolean

    companion object {
        konst emptyFList = object: FList<Any>() {
            public override konst head: Any
                get() = throw UnsupportedOperationException();

            public override konst tail: FList<Any>
                get() = this

            public override konst empty: Boolean
                get() = true
        }
    }

    operator fun plus(head: T): FList<T> = object : FList<T>() {
        override public konst head: T
            get() = head

        override public konst empty: Boolean
            get() = false

        override public konst tail: FList<T>
            get() = this@FList
    }
}

public fun <T> emptyFList(): FList<T> = FList.emptyFList as FList<T>

public fun <T> FList<T>.reverse(where: FList<T> = emptyFList<T>()) : FList<T> =
        if(empty) where else tail.reverse(where + head)

operator fun <T> FList<T>.iterator(): Iterator<T> = object: Iterator<T> {
    private var cur: FList<T> = this@iterator

    override public fun next(): T {
        konst res = cur.head
        cur = cur.tail
        return res
    }
    override public fun hasNext(): Boolean = !cur.empty
}

fun box() : String {
  var r = ""
  for(s in (emptyFList<String>() + "O" + "K").reverse()) {
    r += s
  }
  return r
}
