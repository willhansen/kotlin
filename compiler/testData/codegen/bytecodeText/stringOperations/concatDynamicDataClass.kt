// KOTLIN_CONFIGURATION_FLAGS: STRING_CONCAT=indy-with-constants
// JVM_TARGET: 11
data class A(konst i: Int, konst b: Byte, konst c: Char, konst s: Short, konst f: Float, konst d: Double, konst bo: Boolean, konst l: Long)

// 1 INVOKEDYNAMIC makeConcatWithConstants
// 1 INVOKEDYNAMIC makeConcatWithConstants\(IBCSFDZJ\)Ljava/lang/String;
// 1 "A\(i=\\u0001, b=\\u0001, c=\\u0001, s=\\u0001, f=\\u0001, d=\\u0001, bo=\\u0001, l=\\u0001\)"
