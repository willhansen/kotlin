class Outer<O> {
  inner class A<X>: Super<O> {
      fun <Y> foo(x: X, y: Y): Map<X, Map<Y, O>>

      konst map: Map<X, O>
   }
}

abstract class Super<S> {
    abstract fun fromSuper(): S
}

fun foo(o: Outer<String>) {
    konst a = o.A<Int>()
    println(<expr>a</expr>)
}