class Bar(konst name: String)

abstract class Foo {
  public abstract fun foo(): String
}

fun box(): String {
    return object: Foo() {
      inner class NestedFoo(konst bar: Bar) {
          fun copy(bar: Bar) = NestedFoo(bar)
      }

      override fun foo(): String {
        return NestedFoo(Bar("Fail")).copy(bar = Bar("OK")).bar.name
      }
    }.foo()
}