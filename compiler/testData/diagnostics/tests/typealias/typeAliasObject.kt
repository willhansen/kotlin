// FIR_IDENTICAL
object AnObject {
    konst ok = "OK"
    fun foo() = "OK"
}

typealias TestObject = AnObject

konst test11: AnObject = TestObject
konst test12: TestObject = TestObject
konst test13: String = TestObject.ok
konst test14: String = TestObject.foo()

typealias TestObject2 = TestObject

konst test21: AnObject = TestObject2
konst test22: TestObject2 = TestObject2
konst test23: String = TestObject2.ok
konst test24: String = TestObject2.foo()

class ClassWithCompanion {
    companion object {
        konst ok = "OK"
        fun foo() = "OK"
    }
}

typealias TestCWC = ClassWithCompanion

konst test35: ClassWithCompanion.Companion = TestCWC
konst test36 = TestCWC
konst test37: String = TestCWC.ok
konst test38: String = TestCWC.foo()
