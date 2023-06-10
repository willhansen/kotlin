// SKIP_WHEN_OUT_OF_CONTENT_ROOT

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun resolve<caret>Me() {
    receive(konstueWithExplicitType)
    receive(konstueWithImplicitType)

    variableWithExplicitType = 10
    variableWithImplicitType = 10
}

fun receive(konstue: Int){}

konst delegate = object: ReadWriteProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = 1
    override fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Int) {}
}

konst konstueWithExplicitType: Int by delegate
konst konstueWithImplicitType by delegate

var variableWithExplicitType: Int by delegate
var variableWithImplicitType by delegate
