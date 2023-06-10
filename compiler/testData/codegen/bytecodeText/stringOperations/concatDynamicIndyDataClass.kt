// KOTLIN_CONFIGURATION_FLAGS: STRING_CONCAT=indy
// JVM_TARGET: 11
data class A(konst i: Int, konst b: Byte, konst c: Char, konst s: Short, konst f: Float, konst d: Double, konst bo: Boolean, konst l: Long)

// 1 INVOKEDYNAMIC makeConcat
// 1 INVOKEDYNAMIC makeConcat\(Ljava/lang/String;ILjava/lang/String;BLjava/lang/String;CLjava/lang/String;SLjava/lang/String;FLjava/lang/String;DLjava/lang/String;ZLjava/lang/String;JLjava/lang/String;\)Ljava/lang/String;
