// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 1 java/lang/invoke/LambdaMetafactory

// FILE: enumValues.kt
enum class ABC(konst x: String = "") {
    A("OK"), B, C
}

fun box() =
    Sam(::enumValues).get()[0].x

// FILE: Sam.java
public interface Sam {
    ABC[] get();
}
