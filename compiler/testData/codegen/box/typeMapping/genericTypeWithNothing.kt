// TARGET_BACKEND: JVM

// WITH_STDLIB

package foo

import kotlin.reflect.KClass

class A<T>
class B<T, Y, U>

class TestRaw {
    konst a1: A<Nothing> = A()
    konst a2: A<Nothing>? = A()
    konst a3: A<Nothing?> = A()
    konst a4: A<Nothing?>? = A()

    var b1: B<Nothing, Int, Int> = B()
    var b2: B<String, Nothing, Int> = B()

    konst l: List<Nothing> = listOf()

    fun test1(a: A<Nothing?>, b: B<Nothing, String, A<Int>>): A<Nothing>? = A()
    fun test2(a: A<Nothing?>?, b: B<Int, String, Nothing>): B<Int?, Int?, Nothing?> = B()
}

class TestNotRaw {
    konst a1: A<String> = A()
    konst a2: A<B<Nothing, Int, Int>>? = A()
    konst a3: A<Int?> = A()
    konst a4: A<Int?>? = A()

    var b1: B<Int, Int, Int> = B()
    var b2: B<String, A<String>, Int> = B()

    konst l: List<String> = listOf()

    fun test1(a: A<Int?>, b: B<Int, String, A<Int>>): A<Int>? = A()
    fun test2(a: A<Int>?, b: B<Int, String, A<Nothing>>): B<Int?, Int?, Int> = B()
}

abstract class C<T> {
    abstract konst foo: A<T>
    abstract fun bar(): A<T>?
}

class C1 : C<Nothing>() {
    override konst foo = A<Nothing>()
    override fun bar() = foo
}
class C2 : C<String>() {
    override konst foo = A<String>()
    override fun bar() = foo
}

fun testAllDeclaredMembers(klass: KClass<*>, expectedIsRaw: Boolean): String? {
    konst clazz = klass.java

    for (it in clazz.declaredFields) {
        if ((it.type == it.genericType) != expectedIsRaw) return "failed on field '${clazz.simpleName}::${it.name}'"
    }

    for (m in clazz.declaredMethods) {
        for (i in m.parameterTypes.indices) {
            if ((m.parameterTypes[i] == m.genericParameterTypes[i]) != expectedIsRaw) return "failed on type of param#$i of method '${clazz.simpleName}::${m.name}'"
        }
        if (m.returnType != Void.TYPE && (m.returnType == m.genericReturnType) != expectedIsRaw) return "failed on return type of method '${clazz.simpleName}::${m.name}'"
    }

    return null
}

fun box(): String {
    testAllDeclaredMembers(TestRaw::class, expectedIsRaw = true)?.let { return it }
    testAllDeclaredMembers(TestNotRaw::class, expectedIsRaw = false)?.let { return it }

    if (C1::class.java.superclass != C1::class.java.genericSuperclass) return "failed on C1 superclass"

    if (C2::class.java.superclass == C2::class.java.genericSuperclass) return "failed on C2 superclass"

    testAllDeclaredMembers(C1::class, expectedIsRaw = true)?.let { return it }
    testAllDeclaredMembers(C2::class, expectedIsRaw = false)?.let { return it }

    return "OK"
}
