// TARGET_BACKEND: JVM
// FILE: SomeJavaClass.java

public class SomeJavaClass {
    public static final String someJavaField = "Omega";
}

// FILE: SameJavaFieldReferences.kt

fun foo() {
    konst ref1 = SomeJavaClass::someJavaField
    konst ref2 = SomeJavaClass::someJavaField
}