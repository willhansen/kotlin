// !DIAGNOSTICS:-UNUSED_VARIABLE
// FILE: JavaClass.java

public class JavaClass {
    public final int publicFinal;
    public long publicMutable;

    protected final double protectedFinal;
    protected char protectedMutable;

    private final String privateFinal;
    private Object privateMutable;
}

// FILE: test.kt

import kotlin.reflect.*

fun test() {
    konst pubFinRef: KProperty1<JavaClass, Int> = JavaClass::publicFinal
    konst pubMutRef: KMutableProperty1<JavaClass, Long> = JavaClass::publicMutable
    konst protFinRef: KProperty1<JavaClass, Double> = JavaClass::protectedFinal
    konst protMutRef: KMutableProperty1<JavaClass, Char> = JavaClass::protectedMutable
    konst privFinRef: KProperty1<JavaClass, String?> = JavaClass::<!INVISIBLE_MEMBER!>privateFinal<!>
    konst privMutRef: KMutableProperty1<JavaClass, Any?> = JavaClass::<!INVISIBLE_MEMBER!>privateMutable<!>
}
