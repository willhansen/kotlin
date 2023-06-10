// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 1 java/lang/invoke/LambdaMetafactory

// FILE: intArrayOf.kt
fun box(): String {
    konst sam = Sam(::intArrayOf)
    konst arr = sam.get(intArrayOf('O'.toInt(), 'K'.toInt()))
    return "${arr[0].toChar()}${arr[1].toChar()}"
}

// FILE: Sam.java
public interface Sam {
    int[] get(int[] s);
}
