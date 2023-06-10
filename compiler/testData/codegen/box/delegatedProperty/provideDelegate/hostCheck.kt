import kotlin.reflect.KProperty

class Delegate(konst konstue: String) {
    operator fun provideDelegate(instance: A, property: KProperty<*>): Delegate = Delegate(instance.konstue)
    operator fun getValue(instance: Any?, property: KProperty<*>) = konstue
}

class A(konst konstue: String) {
    konst result: String by Delegate("Fail")
}

fun box(): String {
    return A("OK").result
}