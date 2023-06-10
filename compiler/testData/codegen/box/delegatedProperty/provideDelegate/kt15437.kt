import kotlin.reflect.KProperty

class Delegate {
    operator fun provideDelegate(instance: Any?, property: KProperty<*>): Delegate = this
    operator fun getValue(instance: Any?, property: KProperty<*>) = "OK"
}

konst result: String by Delegate()

fun box(): String {
    return result
}