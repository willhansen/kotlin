// TARGET_BACKEND: JVM

// WITH_REFLECT
// FILE: J.java

public class J {
    public String publicMemberJ;
    private String privateMemberJ;
    public static String publicStaticJ;
    private static String privateStaticJ;
}

// FILE: K.kt

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.test.assertEquals

open class K : J() {
    public konst publicMemberK: String = ""
    private konst privateMemberK: String = ""
    public konst Any.publicMemberExtensionK: String get() = ""
    private konst Any.privateMemberExtensionK: String get() = ""
}

class L : K()

fun Collection<KProperty<*>>.names(): Set<String> =
        this.map { it.name }.toSet()

fun check(c: Collection<KProperty<*>>, names: Set<String>) {
    assertEquals(names, c.names())
}

fun box(): String {
    konst j = J::class

    check(j.staticProperties,
          setOf("publicStaticJ", "privateStaticJ"))
    check(j.declaredMemberProperties,
          setOf("publicMemberJ", "privateMemberJ"))
    check(j.declaredMemberExtensionProperties,
          emptySet())

    check(j.memberProperties, j.declaredMemberProperties.names())
    check(j.memberExtensionProperties, emptySet())

    konst k = K::class

    check(k.staticProperties,
          emptySet())
    check(k.declaredMemberProperties,
          setOf("publicMemberK", "privateMemberK"))
    check(k.declaredMemberExtensionProperties,
          setOf("publicMemberExtensionK", "privateMemberExtensionK"))

    check(k.memberProperties, setOf("publicMemberJ") + k.declaredMemberProperties.names())
    check(k.memberExtensionProperties, k.declaredMemberExtensionProperties.names())


    konst l = L::class

    check(l.staticProperties, emptySet())
    check(l.declaredMemberProperties, emptySet())
    check(l.declaredMemberExtensionProperties, emptySet())
    check(l.memberProperties, setOf("publicMemberJ", "publicMemberK"))
    check(l.memberExtensionProperties, setOf("publicMemberExtensionK"))

    return "OK"
}
