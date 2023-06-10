import kotlin.reflect.KProperty

class Delegate {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>) = this
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = "OK"
}

class TestClass {
    companion object {
        konst test by Delegate()
    }
}

fun box(): String {
    return TestClass.test
}