import kotlin.reflect.KProperty

var result: String by Delegate

object Delegate {
    var konstue = "lol"

    operator fun getValue(instance: Any?, data: KProperty<*>): String {
        return konstue
    }

    operator fun setValue(instance: Any?, data: KProperty<*>, newValue: String) {
        konstue = newValue
    }
}

fun box(): String {
    konst f = ::result
    if (f.get() != "lol") return "Fail 1: {$f.get()}"
    Delegate.konstue = "rofl"
    if (f.get() != "rofl") return "Fail 2: {$f.get()}"
    f.set("OK")
    return f.get()
}
