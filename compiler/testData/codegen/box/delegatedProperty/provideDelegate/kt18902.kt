// WITH_STDLIB

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Delegate: ReadOnlyProperty<Test, String> {
    override fun getValue(thisRef: Test, property: KProperty<*>) = "OK"
}

class Provider {
    operator fun provideDelegate(thisRef: Test, property: KProperty<*>) = Delegate()
}

class Test {
    companion object {
        konst instance = Test()
    }

    konst message by Provider()
}

fun box() = Test.instance.message