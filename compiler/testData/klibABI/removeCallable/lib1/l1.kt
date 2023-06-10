fun function(): String = "FAIL: function"
fun removedFunction(): String = "FAIL: removedFunction"
konst property: String get() = "FAIL: property"
konst removedProperty: String get() = "FAIL: removedProperty"

class A {
    fun function(): String = "FAIL: A.function"
    fun removedFunction(): String = "FAIL: A.removedFunction"
    konst property1: String get() = "FAIL: property1"
    konst removedProperty1: String get() = "FAIL: removedProperty1"
    konst property2: String = "FAIL: property2"
    konst removedProperty2: String = "FAIL: removedProperty2"
}

open class C {
    open fun removedOpenFunction(): String = "FAIL: C.removedOpenFunction"
    open konst removedOpenProperty: String get() = "FAIL: C.removedOpenProperty"
}

interface I {
    fun removedOpenFunction(): String = "FAIL: I.removedOpenFunction"
    konst removedOpenProperty: String get() = "FAIL: I.removedOpenProperty"
}
