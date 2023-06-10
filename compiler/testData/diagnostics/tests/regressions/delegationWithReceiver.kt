// FIR_IDENTICAL
import kotlin.reflect.KProperty

class MyMetadata<in T, R>(konst default: R) {
    operator fun getValue(thisRef: T, desc: KProperty<*>): R = TODO()
    operator fun setValue(thisRef: T, desc: KProperty<*>, konstue: R) {}
}

interface Something
class MyReceiver
var MyReceiver.something: Something? by MyMetadata(default = null)
