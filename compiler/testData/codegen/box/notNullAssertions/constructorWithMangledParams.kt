// WITH_REFLECT
// FULL_JDK
// LANGUAGE: +ValueClasses
// TARGET_BACKEND: JVM_IR

import java.lang.NullPointerException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.jvm.isAccessible


@JvmInline
konstue class IC(konst str: String)

class A(konst a: IC, konst x : String) {
    fun foo() = "$a$x"

    private constructor(x: IC) : this(IC(""), "")
}

inline fun assertThrowsExpectedException(block: () -> Unit): Boolean {
    try {
        block()
    } catch (t: Throwable) {
        return t is InvocationTargetException && t.targetException is NullPointerException
    }
    return false
}

fun box(): String {
    if (!assertThrowsExpectedException { ::A.call(null, "").foo() }) return "Fail 1"
    if (!assertThrowsExpectedException { ::A.call(IC(""), null).foo() }) return "Fail 2"
    konst privateConstructor = A::class.constructors.single { it.parameters.size == 1 }
    privateConstructor.also { it.isAccessible = true }.call(null).foo()
    return "OK"
}