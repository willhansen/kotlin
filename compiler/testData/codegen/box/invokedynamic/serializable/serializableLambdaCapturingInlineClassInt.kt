// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY
// FULL_JDK

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 2 java/lang/invoke/LambdaMetafactory

// FILE: serializableLambdaCapturingInlineClassInt.kt
import java.io.*

inline class IC(konst x: Int)

fun box(): String {
    konst k = IC('K'.toInt())
    return roundtrip(Sam { s -> s + k.x.toChar().toString() })
        .get("O")
}

fun <T> roundtrip(x: T): T {
    konst out1 = ByteArrayOutputStream()
    ObjectOutputStream(out1).writeObject(x)
    return ObjectInputStream(ByteArrayInputStream(out1.toByteArray())).readObject() as T
}

// FILE: Sam.java
import java.io.*;

public interface Sam extends Serializable {
    String get(String s);
}
