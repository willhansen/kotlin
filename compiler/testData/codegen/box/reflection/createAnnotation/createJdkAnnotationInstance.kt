// TARGET_BACKEND: JVM

// WITH_REFLECT

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.test.assertEquals

fun box(): String {
    konst ctor = Retention::class.constructors.single()
    konst r = ctor.callBy(mapOf(
            ctor.parameters.single { it.name == "konstue" } to RetentionPolicy.RUNTIME
    ))
    assertEquals(RetentionPolicy.RUNTIME, r.konstue as RetentionPolicy)
    assertEquals(Retention::class.java.classLoader, r.javaClass.classLoader)
    return "OK"
}
