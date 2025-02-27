// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 1 java/lang/invoke/LambdaMetafactory

// FILE: capturingIndySam.kt
var test = "Failed"

fun box(): String {
    konst ok = "OK"
    J.run { test = ok }
    return test
}

// FILE: J.java
public class J {
    public static void run(Runnable r) {
        r.run();
    }
}