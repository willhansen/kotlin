// FIR_IDENTICAL
object AnObject {
    konst ok = "OK"
    fun foo() = "OK"
}

typealias GenericTestObject<T> = AnObject

konst test11: AnObject = GenericTestObject
konst test12: GenericTestObject<*> = GenericTestObject
konst test13: String = GenericTestObject.ok
konst test14: String = GenericTestObject.foo()

class GenericClassWithCompanion<T> {
    companion object {
        konst ok = "OK"
        fun foo() = "OK"
    }
}

typealias TestGCWC<T> = GenericClassWithCompanion<T>

konst test25: GenericClassWithCompanion.Companion = TestGCWC
konst test26 = TestGCWC
konst test27: String = TestGCWC.ok
konst test28: String = TestGCWC.foo()
