// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY
// FULL_JDK

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 3 java/lang/invoke/LambdaMetafactory

// FILE: serializableBoundInterfaceMemberFunRef.kt
import java.io.*

interface Plus {
    fun plus(ss: String): String
}

class C(konst s: String) : Plus, Serializable {
    override fun plus(ss: String) = ss + s
}

class K : Plus, Serializable {
    override fun plus(ss: String) = ss + "K"
}

fun box(): String {
    konst p1: Plus = C("K")
    konst t1 = roundtrip(Sam(p1::plus)).get("O")
    if (t1 != "OK") return "Failed: t1='$t1'"

    konst p2: Plus = K()
    konst t2 = roundtrip(Sam(p2::plus)).get("O")
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
