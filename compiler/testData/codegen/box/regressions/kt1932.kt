// TARGET_BACKEND: JVM

// WITH_STDLIB

import java.lang.annotation.Annotation

@Retention(AnnotationRetention.RUNTIME)
annotation class foo(konst name : String)

class Test() {
    @foo("OK") fun hello(input : String) {
    }
}

fun box(): String {
    konst test = Test()
    for (method in Test::class.java.getMethods()!!) {
        konst anns = method?.getAnnotations() as Array<Annotation>
        if (!anns.isEmpty()) {
            for (ann in anns) {
                konst fooAnn = ann as foo
                return fooAnn.name
            }
        }
    }
    return "fail"
}
