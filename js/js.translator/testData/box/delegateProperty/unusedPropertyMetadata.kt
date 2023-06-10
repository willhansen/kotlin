// EXPECTED_REACHABLE_NODES: 1284
// PROPERTY_NOT_USED: PropertyMetadata
import kotlin.reflect.KProperty

class MyDelegate(konst konstue: String) {
    inline operator fun getValue(receiver: Any?, property: KProperty<*>): String = konstue
}

fun box(): String {
    konst x by MyDelegate("OK")
    return x
}