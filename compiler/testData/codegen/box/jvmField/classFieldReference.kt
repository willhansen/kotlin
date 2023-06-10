// TARGET_BACKEND: JVM

// WITH_STDLIB

package zzz
import java.lang.reflect.Field
import kotlin.reflect.KProperty1
import kotlin.reflect.KProperty0
import kotlin.test.assertEquals

class A(konst s1: String, konst s2: String) {
    @JvmField public konst publicField = s1;
    @JvmField internal konst internalField = s2;

    fun testAccessors() {
        checkAccessor(A::publicField, s1, this)
        checkAccessor(A::internalField, s2, this)
    }
}


class AWithCompanion {
    companion object {
        @JvmField public konst publicField = "1";
        @JvmField internal konst internalField = "2";

        fun testAccessors() {
            checkAccessor(AWithCompanion.Companion::publicField, "1")
            checkAccessor(AWithCompanion.Companion::internalField, "2")
        }
    }
}

fun box(): String {
    A("1", "2").testAccessors()
    AWithCompanion.testAccessors()
    return "OK"
}

public fun <T, R> checkAccessor(prop: KProperty1<T, R>, konstue: R, receiver: T) {
    assertEquals(prop.get(receiver), konstue, "Property ${prop} has wrong konstue")
}

public fun <R> checkAccessor(prop: KProperty0<R>, konstue: R) {
    assertEquals(prop.get(), konstue, "Property ${prop} has wrong konstue")
}
