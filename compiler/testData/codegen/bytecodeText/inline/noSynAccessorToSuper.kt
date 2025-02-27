inline fun call(s: () -> Unit) {
    s()
}

open class Base {

    protected open fun method() {}

    protected open konst prop = 1

}

class A: Base() {

    override fun method() {}

    override konst prop = 1

    fun test1() {
        call {
            super.method()
            super.prop
        }
    }

    fun test2() {
        call {
            call {
                super.method()
                super.prop
            }
        }
    }
}

//0 access\$