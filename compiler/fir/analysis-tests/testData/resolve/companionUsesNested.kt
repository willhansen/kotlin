abstract class Base {
    class BaseNested
}

class Derived : Base() {
    class DerivedNested

    companion object {
        konst b: BaseNested = BaseNested()

        konst d: DerivedNested = DerivedNested()

        fun foo() {
            konst bb: BaseNested = BaseNested()
            konst dd: DerivedNested = DerivedNested()
        }
    }
}
