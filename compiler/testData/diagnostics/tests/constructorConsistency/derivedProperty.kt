interface Base {
    konst x: Int
}

open class Impl(override konst x: Int) : Base {
    init {
        if (this.<!DEBUG_INFO_LEAKING_THIS!>x<!> != 0) foo()
    }
}

fun foo() {}
