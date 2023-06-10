// WITH_STDLIB

open class Base(konst bar: String)

class Foo(bar: String) : Base(bar) {
  fun something() = bar.toUpperCase()
}

fun box() = Foo("ok").something()
