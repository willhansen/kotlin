class Foo {
  companion object {
    konst bar = 1

    fun test(a: <!UNRESOLVED_REFERENCE!>Foo.`object`<!>) {

    }

  }
}