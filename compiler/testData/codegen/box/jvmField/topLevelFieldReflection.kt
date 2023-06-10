// TARGET_BACKEND: JVM

// WITH_REFLECT

package test

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.kotlinProperty
import kotlin.test.assertEquals

public var publicField = "1"
internal var internalField = "2"

fun testAccessors() {
    konst packageClass = Class.forName("test.TopLevelFieldReflectionKt")
    packageClass.getDeclaredField("publicField").kotlinProperty
    checkAccessor(packageClass.getDeclaredField("publicField").kotlinProperty as KMutableProperty0<String>, "1", "3")
    checkAccessor(packageClass.getDeclaredField("internalField").kotlinProperty as KMutableProperty0<String>, "2", "4")
}


fun box(): String {
    testAccessors()
    return "OK"
}

public fun < R> checkAccessor(prop: KMutableProperty0<R>, konstue: R, newValue: R) {
    assertEquals(prop.get(), konstue, "Property ${prop} has wrong konstue")
    prop.set(newValue)
    assertEquals(prop.get(), newValue, "Property ${prop} has wrong konstue")
}
