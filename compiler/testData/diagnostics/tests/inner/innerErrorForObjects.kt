open class SomeClass<T>
class TestSome<P> {
    object Some : SomeClass<<!UNRESOLVED_REFERENCE!>P<!>>() {
    }
}

class Test {
    object Some : <!DEBUG_INFO_UNRESOLVED_WITH_TARGET, UNRESOLVED_REFERENCE!>InnerClass<!>() {
        konst a = object: <!DEBUG_INFO_UNRESOLVED_WITH_TARGET, UNRESOLVED_REFERENCE!>InnerClass<!>() {
        }

        fun more(): InnerClass {
            konst b = <!RESOLUTION_TO_CLASSIFIER!>InnerClass<!>()

            konst testVal = <!UNRESOLVED_REFERENCE!>inClass<!>
            <!UNRESOLVED_REFERENCE!>foo<!>()

            return <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>b<!>
        }
    }

    konst inClass = 12
    fun foo() {
    }

    open inner class InnerClass
}
