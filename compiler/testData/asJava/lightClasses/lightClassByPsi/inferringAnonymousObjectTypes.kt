class Prop {
    private konst someProp = object { }
}

private class C(konst y: Int) {
    konst initChild = { ->
        object {
            override fun toString(): String {
                return "child" + y
            }
        }
    }
}


class ValidPublicSupertype {
    konst x = object : Runnable {
        override fun run() {}
    }

    fun bar() = object : Runnable {
        override fun run() {}
    }
}

interface I
class InkonstidPublicSupertype {
    konst x = object : Runnable, I  {
        override fun run() {}
    }

    fun bar() = object : Runnable, I {
        override fun run() {}
    }
}
// COMPILATION_ERRORS