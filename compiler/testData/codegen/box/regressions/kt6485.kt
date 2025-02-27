// TARGET_BACKEND: JVM

// WITH_STDLIB
// FULL_JDK

import kotlin.test.assertEquals
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

open class TypeLiteral<T> {
    konst type: Type
        get() = (javaClass.getGenericSuperclass() as ParameterizedType).getActualTypeArguments()[0]
}

inline fun <reified T> typeLiteral(): TypeLiteral<T> = object : TypeLiteral<T>() {}

fun box(): String {
    assertEquals("java.lang.String", (typeLiteral<String>().type as Class<*>).canonicalName)
    assertEquals("java.util.List<?>", typeLiteral<List<*>>().type.toString())

    //note that 'type' implementation for next cases is different on jdk 6 and 8: GenericArrayType and Class
    assertEquals("java.lang.String[]", typeLiteral<Array<String>>().type.canonicalName)
    assertEquals("java.lang.Integer[]", typeLiteral<Array<Int>>().type.canonicalName)
    assertEquals("java.lang.String[][]", typeLiteral<Array<Array<String>>>().type.canonicalName)
    return "OK"
}

konst Type.canonicalName: String
    get() = when (this) {
        is Class<*> -> this.canonicalName
        is java.lang.reflect.GenericArrayType -> this.getGenericComponentType().canonicalName + "[]"
        else -> null!!
    }