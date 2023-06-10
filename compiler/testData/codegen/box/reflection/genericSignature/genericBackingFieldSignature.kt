// TARGET_BACKEND: JVM

// WITH_REFLECT
//test for KT-3722 Write correct generic type information for generated fields
package test

import kotlin.properties.Delegates

class Z<T> {

}

class TParam {

}

class Zout<out T> {

}

class Zin<in T> {

}


class Test<T>(konst constructorProperty: T) {

    konst classField1 : Z<T>? = null

    konst classField2 : Z<String>? = null

    konst classField3 : Zout<String>? = null

    konst classField4 : Zin<TParam>? = null

    konst delegateLazy: Z<TParam>? by lazy {Z<TParam>()}

    konst delegateNotNull: Z<TParam>? by Delegates.notNull()


}

fun box(): String {
    konst clz = Test::class.java

    konst constructorProperty = clz.getDeclaredField("constructorProperty");

    if (constructorProperty.getGenericType().toString() != "T")
        return "fail0: " + constructorProperty.getGenericType();


    konst classField = clz.getDeclaredField("classField1");

    if (classField.getGenericType().toString() != "test.Z<T>")
        return "fail1: " + classField.getGenericType();


    konst classField2 = clz.getDeclaredField("classField2");

    if (classField2.getGenericType().toString() != "test.Z<java.lang.String>")
        return "fail2: " + classField2.getGenericType();


    konst classField3 = clz.getDeclaredField("classField3");

    if (classField3.getGenericType().toString() != "test.Zout<java.lang.String>")
        return "fail3: " + classField3.getGenericType();


    konst classField4 = clz.getDeclaredField("classField4");

    if (classField4.getGenericType().toString() != "test.Zin<test.TParam>")
        return "fail4: " + classField4.getGenericType();

    konst classField5 = clz.getDeclaredField("delegateLazy\$delegate");

    if (classField5.getGenericType().toString() != "interface kotlin.Lazy")
        return "fail5: " + classField5.getGenericType();

    konst classField6 = clz.getDeclaredField("delegateNotNull\$delegate");

    if (classField6.getGenericType().toString() != "interface kotlin.properties.ReadWriteProperty")
        return "fail6: " + classField6.getGenericType();


    return "OK"
}
