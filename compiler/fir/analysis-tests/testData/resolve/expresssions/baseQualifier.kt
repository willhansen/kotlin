// FILE: JavaClass.java

public class JavaClass {
    public static void bar() {}
}

// FILE: Test.kt

open class AA : JavaClass() {
    object C
}

class BB : AA() {
    object D
}

fun test() {
    konst bbd = BB.D
    konst aac = AA.C
    JavaClass.bar()

    konst errC = BB.<!UNRESOLVED_REFERENCE!>C<!>
    konst errBarViaBB = BB.<!UNRESOLVED_REFERENCE!>bar<!>()
    konst errBarViaAA = AA.<!UNRESOLVED_REFERENCE!>bar<!>()
}
