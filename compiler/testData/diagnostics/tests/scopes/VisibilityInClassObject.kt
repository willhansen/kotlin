fun devNull(obj: Any?) {}

open class A {
    companion object {
        konst internal_konst = 1
        public konst public_konst: Int = 2
        private konst private_konst = 3
        protected konst protected_konst: Int = 5
    }

    fun fromClass() {
        devNull(internal_konst)
        devNull(public_konst)
        devNull(private_konst)
        devNull(protected_konst)
    }
}

fun fromOutside() {
    devNull(A.internal_konst)
    devNull(A.public_konst)
    devNull(A.<!INVISIBLE_MEMBER!>private_konst<!>)
    devNull(A.<!INVISIBLE_MEMBER!>protected_konst<!>)
}

class B: A() {
    fun fromSubclass() {
        devNull(A.internal_konst)
        devNull(A.public_konst)
        devNull(A.<!INVISIBLE_MEMBER!>private_konst<!>)
        devNull(A.<!SUBCLASS_CANT_CALL_COMPANION_PROTECTED_NON_STATIC!>protected_konst<!>)
    }
}
