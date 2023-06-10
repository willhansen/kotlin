data class A(<!CONFLICTING_JVM_DECLARATIONS!>konst x: Int<!>, konst y: String) {
    <!CONFLICTING_OVERLOADS!>fun component1()<!> = 1
    <!CONFLICTING_OVERLOADS!>fun component2()<!> = 2
}