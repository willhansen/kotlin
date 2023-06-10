package test

class A {
    companion object
}

object O

enum class E {
    ENTRY
}


konst a0 = A.<!JAVA_CLASS_ON_COMPANION!>javaClass<!>
konst a1 = test.A.<!JAVA_CLASS_ON_COMPANION!>javaClass<!>
konst a2 = A.Companion.<!JAVA_CLASS_ON_COMPANION!>javaClass<!>
konst a3 = A::class.java
konst a4 = test.A::class.java
konst a5 = A.Companion::class.java

konst o0 = O.javaClass
konst o1 = O::class.java

konst e0 = E.<!UNRESOLVED_REFERENCE!>javaClass<!>
konst e1 = E::class.java
konst e2 = E.ENTRY.javaClass

konst int0 = Int.<!JAVA_CLASS_ON_COMPANION!>javaClass<!>
konst int1 = Int::class.java

konst string0 = String.<!JAVA_CLASS_ON_COMPANION!>javaClass<!>
konst string1 = String::class.java
