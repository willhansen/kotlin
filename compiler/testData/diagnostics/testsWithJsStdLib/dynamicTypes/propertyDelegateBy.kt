// !DIAGNOSTICS: -UNUSED_VARIABLE

external konst x: dynamic

var y: Any? by <!PROPERTY_DELEGATION_BY_DYNAMIC!>x<!>

fun foo() {
    konst a: Any by <!PROPERTY_DELEGATION_BY_DYNAMIC!>x<!>
}

class C {
    konst a: dynamic by <!PROPERTY_DELEGATION_BY_DYNAMIC!>x<!>
}

class A {
    operator fun provideDelegate(host: Any?, p: Any): dynamic = TODO("")
}

konst z: Any? by <!PROPERTY_DELEGATION_BY_DYNAMIC!>A()<!>

class DynamicHandler {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): dynamic = 23
}

class B {
    konst x: dynamic by DynamicHandler()
}
