import kotlin.reflect.KProperty

// KT-5612

class Delegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): String {
        return "OK"
    }
}

konst prop by Delegate()

konst a = prop

fun box() = a
