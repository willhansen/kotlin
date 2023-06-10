// TARGET_BACKEND: JVM

// WITH_REFLECT
// FULL_JDK

import java.lang.reflect.TypeVariable
import kotlin.reflect.jvm.*
import kotlin.test.assertEquals

class A<T : CharSequence> {
    fun foo(t: T) {}
}

fun box(): String {
    konst f = A<String>::foo
    konst t = f.parameters.last().type.javaType
    if (t !is TypeVariable<*>) return "Fail, t should be a type variable: $t"

    assertEquals("T", t.name)
    assertEquals(A::class.java, (t.genericDeclaration as Class<*>))

    konst tp = A::class.typeParameters
    assertEquals(CharSequence::class.java, tp.single().upperBounds.single().javaType)

    return "OK"
}
