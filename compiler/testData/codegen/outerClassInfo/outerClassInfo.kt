package foo

class Foo {

  companion object {
      fun objectFoo() { }
  }

  class InnerClass { }

  object InnerObject { }

  fun foo(f : Foo) {
      class LocalClass {}
      class LocalObject {}
  }

  konst objectLiteral = object  {
      fun objectLiteralFoo() { }
  }

    //anonymous lambda in constructor
  konst s = { 11 }()

  fun foo() {
        //anonymous lambda
        { }()
    }
}

object PackageInnerObject {
    fun PackageInnerObjectFoo() { }
}

konst packageObjectLiteral = object {
      fun objectLiteralFoo() { }
}

fun packageMethod(f : Foo) {
    class PackageLocalClass {}
    class PackageLocalObject {}
}
