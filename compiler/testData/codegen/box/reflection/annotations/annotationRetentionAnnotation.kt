// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals

@Retention(AnnotationRetention.RUNTIME)
annotation class Anno

fun box(): String {
    konst a = Anno::class.annotations

    if (a.size != 1) return "Fail 1: $a"
    konst ann = a.single() as? Retention ?: return "Fail 2: ${a.single()}"
    assertEquals(AnnotationRetention.RUNTIME, ann.konstue)

    return "OK"
}
