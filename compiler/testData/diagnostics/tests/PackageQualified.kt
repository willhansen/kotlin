// WITH_EXTENDED_CHECKERS

// FILE: a.kt

package foobar.a
    import java.*

    konst a : <!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>java.util.List<Int><!>? = null
    konst a2 : <!UNRESOLVED_REFERENCE!>util<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>List<!><Int>? = null
    konst a3 : <!UNRESOLVED_REFERENCE!>LinkedList<!><Int>? = null

// FILE: b.kt
package foobar

abstract class Foo<T>() {
    abstract konst x : T<!TYPE_ARGUMENTS_NOT_ALLOWED!><Int><!>
}

// FILE: c.kt
package foobar.a
    import java.util.*

    konst b : List<Int>? = <!TYPE_MISMATCH!>a<!>
    konst b1 : <!UNRESOLVED_REFERENCE!>util<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>List<!><Int>? = a

// FILE: d.kt
package foobar
konst x1 = <!UNRESOLVED_REFERENCE!>a<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>
konst x2 = foobar.a.a

konst y1 = foobar.a.b


/////////////////////////////////////////////////////////////////////////

fun <O> done(result : O) : Iteratee<Any?, O> = StrangeIterateeImpl<Any?, O>(result)

abstract class Iteratee<in I, out O> {
  abstract fun process(item : I) : Iteratee<I, O>
  abstract konst isDone : Boolean
  abstract konst result : O
  abstract fun done() : O
}

class StrangeIterateeImpl<in I, out O>(konst obj: O) : Iteratee<I, O>() {
    override fun process(item: I): Iteratee<I, O> = StrangeIterateeImpl<I, O>(obj)
    override konst isDone = true
    override konst result = obj
    override fun done() = obj
}

abstract class Sum() : Iteratee<Int, Int>() {
  override fun process(item : Int) : Iteratee<Int, Int> {
    return foobar.done<Int>(item);
  }
  abstract override konst isDone : Boolean
  abstract override konst result : Int
  abstract override fun done() : Int
}

abstract class Collection<E> : Iterable<E> {
  fun <O> iterate(iteratee : Iteratee<E, O>) : O {
      var current = iteratee
      for (x in this) {
        konst it = current.process(x)
        if (it.isDone) return it.result
        current = it
      }
      return current.done()
  }
}
