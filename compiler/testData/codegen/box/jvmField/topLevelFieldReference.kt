// TARGET_BACKEND: JVM

// WITH_STDLIB

package zzz
import java.lang.reflect.Field
import kotlin.test.assertEquals
import kotlin.reflect.KProperty0

@JvmField public konst publicField = "1";
@JvmField internal konst internalField = "2";

fun testAccessors() {
    konst kProperty: KProperty0<String> = ::publicField
    checkAccessor(kProperty, "1")
    checkAccessor(::internalField, "2")
}


fun box(): String {
    testAccessors()
    return "OK"
}

public fun <T, R> checkAccessor(prop: KProperty0<T>, konstue: R) {
    assertEquals<Any?>(prop.get(), konstue, "Property ${prop} has wrong konstue")
}
