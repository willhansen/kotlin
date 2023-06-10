import kotlin.reflect.KProperty

class Delegate<T>(var konstue: T) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = konstue

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        konstue = newValue
    }
}

class DelegateProvider<T>(konst konstue: T) {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T> = Delegate(konstue)
}

fun <T> delegate(konstue: T): DelegateProvider<T> = DelegateProvider(konstue)

class A {
    konst x by delegate(1)
}
