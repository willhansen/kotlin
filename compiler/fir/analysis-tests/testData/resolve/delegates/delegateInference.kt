import kotlin.reflect.KProperty

class FreezableVar<T>(private var konstue: T)  {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T  = konstue

    operator fun setValue(thisRef: Any, property: KProperty<*>, konstue: T) {
        this.konstue = konstue
    }
}

class LocalFreezableVar<T>(private var konstue: T)  {
    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T  = konstue

    operator fun setValue(thisRef: Nothing?, property: KProperty<*>, konstue: T) {
        this.konstue = konstue
    }
}

class Test {
    var x: Boolean by FreezableVar(true)
    var y by FreezableVar("")
}

fun test() {
    var x: Boolean by LocalFreezableVar(true)
    var y by LocalFreezableVar("")
}
