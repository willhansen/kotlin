// !LANGUAGE: -ReferencesToSyntheticJavaProperties
// FIR_IDENTICAL

// FILE: AnnInterface.java
public @interface AnnInterface {
    public String javaMethod() default "";
}

// FILE: Main.kt
konst prop = AnnInterface::javaMethod
