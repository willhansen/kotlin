import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(thisRef: Test, property: KProperty<*>) = "OK"
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

konst x = Test.instance.message

// expected: x: OK