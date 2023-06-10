import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

konst x: Int by lazy { 1 + 2 }

konst delegate = object: ReadWriteProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = 1
    override fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Int) {}
}

konst konstue by delegate

var variable by delegate

interface Base {
}

class Derived(b: Base) : Base by b {
}
