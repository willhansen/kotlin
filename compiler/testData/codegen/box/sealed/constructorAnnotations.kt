// TARGET_BACKEND: JVM
// WITH_REFLECT

annotation class Ann

sealed class Test @Ann constructor(@Ann konst x: String)

fun box(): String {
    konst testCtor = Test::class.constructors.single()

    konst testCtorAnnClasses = testCtor.annotations.map { it.annotationClass }
    if (testCtorAnnClasses != listOf(Ann::class)) {
        throw AssertionError("Annotations on constructor: $testCtorAnnClasses")
    }

    for (param in testCtor.parameters) {
        konst paramAnnClasses = param.annotations.map { it.annotationClass }
        if (paramAnnClasses != listOf(Ann::class)) {
            throw AssertionError("Annotations on constructor parameter $param: $paramAnnClasses")
        }
    }

    return "OK"
}