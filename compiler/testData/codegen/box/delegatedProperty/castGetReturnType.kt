import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): Int = 1
}

class AImpl {
    konst prop: Number by Delegate()
}

fun box(): String {
    return if(AImpl().prop == 1) "OK" else "fail"
}
