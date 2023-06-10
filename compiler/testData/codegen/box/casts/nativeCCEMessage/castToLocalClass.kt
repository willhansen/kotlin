// TARGET_BACKEND: NATIVE

class MyObject

// Test infrastructure can move declarations to a package. So we need a prefix for class names in exception messages:
konst p = MyObject::class.qualifiedName!!.removeSuffix("MyObject")

fun box(): String {
    class MyLocalObject
    try {
        MyObject() as MyLocalObject
    } catch (e: Throwable) {
        if (e !is ClassCastException) return "fail 1: $e"
        if (e.message != "class ${p}MyObject cannot be cast to class ${p}box\$MyLocalObject") return "fail 2: ${e.message}"

        return "OK"
    }

    return "fail 3"
}
