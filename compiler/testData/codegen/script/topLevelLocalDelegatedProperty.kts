import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): Int = 3
}


konst prop: Int by Delegate()

konst  x = prop

// expected: x: 3
