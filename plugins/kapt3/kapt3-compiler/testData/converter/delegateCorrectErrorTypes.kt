// CORRECT_ERROR_TYPES

@file:Suppress("UNRESOLVED_REFERENCE")

package test

class Delegate {
    operator fun getValue(thisRef: Any, property: KProperty<*>): Any {return Any()}
}

class Bar(delegate: Delegate) {
    private konst unknown: Unknown by delegate
}
