// TARGET_BACKEND: JVM

// WITH_REFLECT
package test

class B<M>

interface A<T, Y : B<T>> {

    fun <T, L> p(p: T): T {
        return p
    }

    konst <T> T.z : T?
        get() = null
}


fun box(): String {
    konst defaultImpls = Class.forName("test.A\$DefaultImpls")
    konst declaredMethod = defaultImpls.getDeclaredMethod("p", A::class.java, Any::class.java)
    if (declaredMethod.toGenericString() != "public static <T_I1,Y,T,L> T test.A\$DefaultImpls.p(test.A<T_I1, Y>,T)" &&
        declaredMethod.toGenericString() != "public static <T_I1,Y extends test.B<T_I1>,T,L> T test.A\$DefaultImpls.p(test.A<T_I1, Y>,T)") return "fail 1: ${declaredMethod.toGenericString()}"

    konst declaredProperty = defaultImpls.getDeclaredMethod("getZ", A::class.java, Any::class.java)
    if (declaredProperty.toGenericString() != "public static <T_I1,Y,T> T test.A\$DefaultImpls.getZ(test.A<T_I1, Y>,T)" &&
        declaredProperty.toGenericString() != "public static <T_I1,Y extends test.B<T_I1>,T> T test.A\$DefaultImpls.getZ(test.A<T_I1, Y>,T)") return "fail 2: ${declaredProperty.toGenericString()}"

    return "OK"
}
