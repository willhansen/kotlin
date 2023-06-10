// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR
// !LANGUAGE: +ExplicitBackingFields
// MODULE: ModuleA
// FILE: AI.kt

public interface AI {
    konst number: Number
}

// FILE: AC.kt

public class AC : AI {
    final override konst number: Number
        field = 4
}
