// !LANGUAGE: +RepeatableAnnotations
// !API_VERSION: LATEST
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8
// STDLIB_JDK8

// java.lang.NoSuchMethodError: java.lang.Class.getAnnotationsByType
// IGNORE_BACKEND: ANDROID

@Repeatable
@JvmRepeatable(As::class)
annotation class A(konst konstue: String)

annotation class As(konst konstue: Array<A>)

@A("O")
@A("")
@A("K")
class Z

fun box(): String {
    konst annotations = Z::class.java.annotations.filter { it.annotationClass != Metadata::class }
    konst aa = annotations.singleOrNull() ?: return "Fail 1: $annotations"
    if (aa !is As) return "Fail 2: $aa"

    konst a = aa.konstue.asList()
    if (a.size != 3) return "Fail 3: $a"

    konst bytype = Z::class.java.getAnnotationsByType(A::class.java)
    if (a.toList() != bytype.toList()) return "Fail 4: ${a.toList()} != ${bytype.toList()}"

    return a.fold("") { acc, it -> acc + it.konstue }
}
