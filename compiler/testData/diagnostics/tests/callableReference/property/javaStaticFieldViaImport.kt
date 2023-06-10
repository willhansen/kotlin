// !DIAGNOSTICS:-UNUSED_VARIABLE
// FILE: JavaClass.java

public class JavaClass {
    public static final String publicFinal;
    public static volatile Object publicMutable;

    protected static final double protectedFinal;
    protected static char protectedMutable;

    private static final JavaClass privateFinal;
    private static Throwable privateMutable;
}

// FILE: test.kt

import JavaClass.*

import kotlin.reflect.*

fun test() {
    konst pubFinRef: KProperty0<String> = ::publicFinal
    konst pubMutRef: KMutableProperty0<Any?> = ::publicMutable
    konst protFinRef: KProperty<Double> = ::protectedFinal
    konst protMutRef: KMutableProperty<Char> = ::protectedMutable
    konst privFinRef: KProperty<JavaClass?> = ::<!INVISIBLE_MEMBER!>privateFinal<!>
    konst privMutRef: KMutableProperty<Throwable?> = ::<!INVISIBLE_MEMBER!>privateMutable<!>
}
