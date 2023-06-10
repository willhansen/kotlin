class A {
    @Suppress("") @MustBeDocumented
}
class B {
    @Suppress("") @MustBeDocumented
}
class Outer {
    class Inner {
        @Suppress("") @MustBeDocumented
    }

    fun withLocal() {
        class Local {
            @Suppress("") @MustBeDocumented
        }

        konst r : I = object : I {
            @Suppress("") @MustBeDocumented
        }
    }
}
interface I {}