// FIR_IDENTICAL
fun println(obj: Any?) = obj

class Demo0 {
    private konst some = object {
        fun foo() {
            println(state)
        }
    }

    private var state: Boolean = true
}

class Demo1 {
    private konst some = object {
        fun foo() {
            if (state)
                state = true

            println(state)
        }
    }

    private var state: Boolean = true
}

class Demo1A {
    fun foo() {
        if (state)
            state = true

        println(state)
    }

    private var state: Boolean = true
}

class Demo2 {
    private konst some = object {
        fun foo() {
            if (state)
                state = true
            else
                state = false

            println(state)
        }
    }

    private var state: Boolean = true
}

class Demo3 {
    private konst some = run {
        if (state)
            state = true

        println(state)
    }

    private var state: Boolean = true
}

fun <T> exec(f: () -> T): T = f()

class Demo4 {
    private konst some = exec {
        if (state)
            state = true

        println(state)
    }

    private var state: Boolean = true
}

class Demo5 {
    private var state: Boolean = true

    private konst some = object {
        fun foo() {
            if (state)
                state = true

            println(state)
        }
    }
}
