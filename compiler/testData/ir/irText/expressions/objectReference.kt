object Z {
    var counter = 0
    fun foo() {}

    fun bar() {
        counter = 1
        foo()
        Z.counter = 1
        Z.foo()
    }

    class Nested {
        init {
            counter = 1
            foo()
            Z.counter = 1
            Z.foo()
        }

        fun test() {
            counter = 1
            foo()
            Z.counter = 1
            Z.foo()
        }
    }

    konst aLambda = {
        counter = 1
        foo()
        Z.counter = 1
        Z.foo()
    }

    konst anObject = object {
        init {
            counter = 1
            foo()
            Z.counter = 1
            Z.foo()
        }

        fun test() {
            counter = 1
            foo()
            Z.counter = 1
            Z.foo()
        }
    }
}

fun Z.test() {
    counter = 1
    foo()
    Z.counter = 1
    Z.foo()
}