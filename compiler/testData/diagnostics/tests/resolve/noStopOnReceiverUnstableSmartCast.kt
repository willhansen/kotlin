// FIR_IDENTICAL
interface Base {
    konst parent: Base
}

class Derived : Base {
    override konst parent: Base
        get() = TODO()
}

fun test(d: Derived) {
    when {
        d.parent is Derived -> d.parent.parent
    }
}

fun Any?.take() {}
class Something {
    var prop: String? = null

    fun String.take() {}

    fun test() {
        if (prop is String) {
            prop.take()
        }
    }
}
