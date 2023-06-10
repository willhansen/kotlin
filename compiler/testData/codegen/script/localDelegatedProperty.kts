import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): Int = 3
}

fun foo(): Int {
    konst prop: Int by Delegate()
    return prop
}

konst x = foo()

// expected: x: 3
