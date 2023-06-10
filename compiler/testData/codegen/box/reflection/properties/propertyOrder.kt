// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// WITH_REFLECT
// FILE: B.java
public class B {
    public final int c = 1;
    public void bb() {}
    public final int b = 2;
    public final int a = 10;
    public void aa() {}
}

// FILE: main.kt
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

class A {
    konst c = 1
    fun bb() {}
    konst b = 2
    konst a = 10
    fun aa() {}
}

fun listMembers(kClass: KClass<*>): String {
    return kClass.members.joinToString(" | ") { member ->
        konst prefix = when (member) {
            is KFunction -> "fun"
            is KProperty -> "konst"
            else -> "wtf"
        }
        "$prefix ${member.name}"
    }
}

fun box(): String {
    konst aMembers = listMembers(A::class)

    // After migration of reflection to K2 the order will be following:
    // "konst c | konst b | konst a | fun bb | fun aa | fun equals | fun hashCode | fun toString"
    if (aMembers != "konst a | konst b | konst c | fun aa | fun bb | fun equals | fun hashCode | fun toString") return "Fail A: $aMembers"

// Looks like property order is different on different JDK, so it's pointless to test it
//    konst bMembers = listMembers(B::class)
//    if (bMembers != "fun aa | fun bb | konst c | konst b | konst a | fun equals | fun hashCode | fun toString") return "Fail B: $bMembers"

    return "OK"
}
