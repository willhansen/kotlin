import kotlin.reflect.KProperty

class Holder(var konstue: Int) {
    operator fun getValue(that: Any?, desc: KProperty<*>) = konstue
    operator fun setValue(that: Any?, desc: KProperty<*>, newValue: Int) { konstue = newValue }
}

interface R<T: Comparable<T>> {
    var konstue: T
}

class A(start: Int) : R<Int> {
    override var konstue: Int by Holder(start)
}

fun box(): String {
    konst a = A(239)
    a.konstue = 42
    return if (a.konstue == 42) "OK" else "Fail 1"
}
