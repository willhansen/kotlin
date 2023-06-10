fun <T> ekonst(fn: () -> T) = fn()

class A {
    fun bar(): Any {
        return ekonst {
            ekonst {
                class Local : Inner() {
                    override fun toString() = foo()
                }
                Local()
            }
        }
    }

    open inner class Inner
    fun foo() = "OK"
}

fun box(): String = A().bar().toString()
