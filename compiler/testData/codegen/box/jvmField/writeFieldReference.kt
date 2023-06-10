// TARGET_BACKEND: JVM

// WITH_STDLIB

package zzz
import kotlin.reflect.KMutableProperty1
import kotlin.test.assertEquals

class A(konst s1: String, konst s2: String) {
    @JvmField public var publicField = s1;
    @JvmField internal var internalField = s2;

    fun testAccessors() {
        konst kMutableProperty: KMutableProperty1<A, String> = A::publicField
        checkAccessor(kMutableProperty, s1, "3", this)
        checkAccessor(A::internalField, s2, "4", this)
    }
}

fun box(): String {
    A("1", "2").testAccessors()
    return "OK"
}

public fun <T, R> checkAccessor(prop: KMutableProperty1<T, R>, konstue: R, newValue: R, receiver: T) {
    assertEquals(prop.get(receiver), konstue, "Property ${prop} has wrong konstue")
    prop.set(receiver, newValue)
    assertEquals(prop.get(receiver), newValue, "Property ${prop} has wrong konstue")
}
