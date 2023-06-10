interface FooTrait {
        konst propertyTest: String
}

class FooDelegate: FooTrait {
        override konst propertyTest: String = "OK"
}

class DelegateTest(): FooTrait by FooDelegate() {
  fun test() = propertyTest
}

fun box()  = DelegateTest().test()
