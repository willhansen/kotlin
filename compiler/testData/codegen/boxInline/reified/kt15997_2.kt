// WITH_REFLECT
// FULL_JDK
// FILE: 1.kt
// TARGET_BACKEND: JVM
package test

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

var result = "fail"

public inline fun <reified T> myObservable(initialValue: T, crossinline onChange: (property: KProperty<*>, oldValue: T, newValue: T) -> Unit):
        ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onChange(property, oldValue, newValue)
}


//samely named reified parameter (T) as in myObservable
inline fun <reified T : Any> crashMe(): ReadWriteProperty<Any?, Unit> {
    return myObservable(Unit, { a, b, c -> result = T::class.java.simpleName })
}


// FILE: 2.kt
import test.*


class OK {
    var konstue by crashMe<OK>()
}

fun box(): String {
    OK().konstue = Unit
    return result
}
