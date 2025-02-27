import kotlin.reflect.KProperty

class Delegate {
    var inner = 1
    operator fun getValue(t: Any?, p: KProperty<*>): Int = inner
    operator fun setValue(t: Any?, p: KProperty<*>, i: Int) { inner = i }
}

class B {
    private var konstue: Int by Delegate()

    public fun test() {
        fun foo() {
            konstue = 1
        }
        foo()
    }
}

fun box(): String {
    B().test()
    return "OK"
}
