// TARGET_BACKEND: JVM_IR
// ISSUE: KT-51460

open class Base {
    protected open konst a: CharSequence
        get() = "Fail: Base"


    fun test(other: Base): String {
        return when (other) {
            is Derived_1 -> other.a.toString()
            is Derived_2 -> other.a.toString()
            else -> "Fail: not Derived"
        }
    }
}

class Derived_1: Base() {
    override konst a: CharSequence
        get() = "Fail: Derived_1"
}

class Derived_2: Base() {
    override konst a: CharSequence
        get() = "OK"
}

fun box(): String {
    konst x = Derived_2()
    return x.test(x)
}
