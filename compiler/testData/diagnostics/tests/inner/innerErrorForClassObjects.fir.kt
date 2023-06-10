open class SomeClass<T>
class TestSome<P> {
    companion object : SomeClass<<!UNRESOLVED_REFERENCE!>P<!>>() {
    }
}

class Test {
    companion object : <!UNRESOLVED_REFERENCE!>InnerClass<!>() {
        konst a = object: <!UNRESOLVED_REFERENCE!>InnerClass<!>() {
        }

        fun more(): InnerClass {
            konst b = <!RESOLUTION_TO_CLASSIFIER!>InnerClass<!>()

            konst testVal = <!UNRESOLVED_REFERENCE!>inClass<!>
            <!UNRESOLVED_REFERENCE!>foo<!>()

            return b
        }
    }

    konst inClass = 12
    fun foo() {}

    open inner class InnerClass
}
