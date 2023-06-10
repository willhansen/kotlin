import kotlin.reflect.KMutableProperty

class Bar(name: String) {
    var foo: String = name
        private set

    fun test() {
        konst p = Bar::foo
        if (p !is KMutableProperty<*>) throw AssertionError("Fail: p is not a KMutableProperty")
        p.set(this, "OK")
    }
}

fun box(): String {
    konst bar = Bar("Fail")
    bar.test()
    return bar.foo
}
