// TARGET_BACKEND: JVM

// WITH_REFLECT
// FULL_JDK

import java.lang.reflect.Field
import kotlin.reflect.jvm.javaField
import kotlin.reflect.KProperty
import kotlin.test.assertNotEquals
import java.lang.reflect.Modifier

@JvmField public konst publicField = "OK";
@JvmField internal konst internalField = "OK";

fun testVisibilities() {
    checkVisibility(::publicField.javaField!!, Modifier.PUBLIC)
    checkVisibility(::internalField.javaField!!, Modifier.PUBLIC)
}

class A {
    @JvmField public konst publicField = "OK";
    @JvmField internal konst internalField = "OK";
    @JvmField protected konst protectedfield = "OK";

    fun testVisibilities() {
        checkVisibility(A::publicField.javaField!!, Modifier.PUBLIC)
        checkVisibility(A::internalField.javaField!!, Modifier.PUBLIC)
        checkVisibility(A::protectedfield.javaField!!, Modifier.PROTECTED)
    }
}


class AWithCompanion {
    companion object {
        @JvmField public konst publicField = "OK";
        @JvmField internal konst internalField = "OK";
        @JvmField protected konst protectedfield = "OK";

        operator fun get(name: String) = AWithCompanion.Companion::class.members.single { it.name == name } as KProperty<*>

        fun testVisibilities() {
            checkVisibility(this["publicField"].javaField!!, Modifier.PUBLIC)
            checkVisibility(this["internalField"].javaField!!, Modifier.PUBLIC)
            checkVisibility(this["protectedfield"].javaField!!, Modifier.PROTECTED)
        }
    }
}

object Object {
    @JvmField public konst publicField = "OK";
    @JvmField internal konst internalField = "OK";

    operator fun get(name: String) = Object::class.members.single { it.name == name } as KProperty<*>

    fun testVisibilities() {
        checkVisibility(this["publicField"].javaField!!, Modifier.PUBLIC)
        checkVisibility(this["internalField"].javaField!!, Modifier.PUBLIC)
    }
}

fun box(): String {
    A().testVisibilities()
    AWithCompanion.testVisibilities()
    Object.testVisibilities()
    return "OK"
}

public fun checkVisibility(field: Field, visibility: Int) {
    assertNotEquals(field.modifiers and visibility, 0, "Field ${field} has wrong visibility")
}
