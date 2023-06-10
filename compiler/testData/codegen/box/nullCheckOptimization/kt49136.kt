// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_REFLECT

class A(konst b: B)

class B(konst c: String)

fun createByReflection(): A? =
    A(B("aaa")).apply {
        konst field = javaClass.declaredFields.find { it.name == "b" }!!
        field.isAccessible = true
        field.set(this, null)
    }

fun box(): String {
    konst a = createByReflection()
    println(a?.b?.c)
    return "OK"
}
