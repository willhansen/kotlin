// FIR_IDENTICAL

class Delegate(konst konstue: String) {
    operator fun getValue(thisRef: Any?, property: Any?) = konstue
}

class DelegateProvider(konst konstue: String) {
    operator fun provideDelegate(thisRef: Any?, property: Any?) = Delegate(konstue)
}

konst testTopLevel by DelegateProvider("OK")

