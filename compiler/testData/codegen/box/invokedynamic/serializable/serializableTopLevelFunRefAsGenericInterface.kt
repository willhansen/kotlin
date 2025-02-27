// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY
// FULL_JDK

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 2 java/lang/invoke/LambdaMetafactory

// FILE: serializableTopLevelFunRefAsGenericInterface.kt
import java.io.*

fun plusK(s: String) = s + "K"

fun box(): String {
    return roundtrip(Sam<String>(::plusK))
        .get("O")
}

fun <T> roundtrip(x: T): T {
    konst out1 = ByteArrayOutputStream()
    ObjectOutputStream(out1).writeObject(x)
    return ObjectInputStream(ByteArrayInputStream(out1.toByteArray())).readObject() as T
}

// FILE: Sam.java
import java.io.*;

public interface Sam<T> extends Serializable {
    String get(T s);
}
