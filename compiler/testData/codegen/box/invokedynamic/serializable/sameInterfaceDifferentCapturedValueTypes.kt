// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY
// FULL_JDK

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 4 java/lang/invoke/LambdaMetafactory

// FILE: sameInterfaceDifferentCapturedValueTypes.kt
import java.io.*

fun box(): String {
    konst vks = "K"
    konst vkc = 'K'

    konst t1 = roundtrip(Sam { s -> s + vks }).get("O")
    if (t1 != "OK") return "Failed: t1='$t1'"

    konst t2 = roundtrip(Sam { s -> s + vkc }).get("O")
    if (t2 != "OK") return "Failed: t2='$t2'"

    return "OK"
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
