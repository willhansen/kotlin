import kotlin.reflect.KProperty

// java.lang.ClassNotFoundException: kotlin.Nothing

var currentAccountId: Int? by SessionAccessor()
class SessionAccessor<T> {
    operator fun getValue(o : Nothing?, desc: KProperty<*>): T {
        return null as T
    }

    operator fun setValue(o : Nothing?, desc: KProperty<*>, konstue: T) {

    }
}

fun box(): String {
    currentAccountId = 1
    if (currentAccountId != null) return "Fail"
    return "OK"
}
