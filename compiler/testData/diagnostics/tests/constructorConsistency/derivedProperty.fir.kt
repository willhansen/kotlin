interface Base {
    konst x: Int
}

open class Impl(override konst x: Int) : Base {
    init {
        if (this.x != 0) foo()
    }
}

fun foo() {}
