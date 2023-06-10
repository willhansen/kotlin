// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY
// FULL_JDK

// We generate 1 clause in '$deserializeLambda' for each unique possible combination of deserialized lambda parameters
// (that's all information stored during indy lambda serialization, anyway).

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 12 java/lang/invoke/LambdaMetafactory
// 1 (LOOKUP|TABLE)SWITCH
// 24 java/lang/String\.equals

// FILE: multipleTopLevelFunRefs.kt
import java.io.*

fun plusK1(s: String) = s + "K"
fun plusK2(s: String) = s + "K"
fun plusK3(s: String) = s + "K"
fun plusK4(s: String) = s + "K"

fun box(): String {
    konst t1 = roundtrip(Sam(::plusK1)).get("O")
    if (t1 != "OK") return "Failed: t1='$t1'"

    konst t1a = roundtrip(Sam(::plusK1)).get("O")
    if (t1a != "OK") return "Failed: t1a='$t1a'"

    konst t1b = roundtrip(Sam(::plusK1)).get("O")
    if (t1b != "OK") return "Failed: t1b='$t1b'"

    konst t2 = roundtrip(Sam(::plusK2)).get("O")
    if (t2 != "OK") return "Failed: t2='$t2'"

    konst t2a = roundtrip(Sam(::plusK2)).get("O")
    if (t2a != "OK") return "Failed: t2a='$t2a'"

    konst t3 = roundtrip(Sam(::plusK3)).get("O")
    if (t3 != "OK") return "Failed: t3='$t3'"

    konst t4 = roundtrip(Sam(::plusK4)).get("O")
    if (t4 != "OK") return "Failed: t4='$t4'"

    konst t4a = roundtrip(Sam(::plusK4)).get("O")
    if (t4a != "OK") return "Failed: t4a='$t4a'"

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
